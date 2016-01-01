package com.yiyangzhu.yydictionary;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DictionaryActivity extends AppCompatActivity {

    private static String TAG = DictionaryActivity.class.getSimpleName();

    @Bind(R.id.input) EditText inputEditText;
    @Bind(R.id.definition) TextView definitionTextView;

    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        ButterKnife.bind(this);

        retrieveYoudaoDictionary();

        inputEditText.setCursorVisible(false);
        inputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEditText.selectAll();
                definitionTextView.setText("");
                inputEditText.setCursorVisible(true);
            }
        });

        final List<String> definitions = new ArrayList<>();
        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    inputEditText.setCursorVisible(false);
                    definitions.clear();
                    final String input = inputEditText.getText().toString().trim();
                    if (input.trim().equals("")) {
                        return true;
                    }

                    final OkHttpClient client = new OkHttpClient();

                    if (YoudaoDictionary.get(input) != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                definitionTextView.setText(buildResultString(
                                        YoudaoDictionary.get(input).getDefinitions()));
                            }
                        });
                    } else {
                        Uri youdaoQuery = Uri.parse("http://fanyi.youdao.com/openapi.do?type=data&doctype=json&version=1.1")
                                .buildUpon()
                                .appendQueryParameter("keyfrom", getString(R.string.youdao_api_keyfrom))
                                .appendQueryParameter("key", getString(R.string.youdao_api_key))
                                .appendQueryParameter("q", input)
                                .build();
                        Log.d(TAG, youdaoQuery.toString());
                        Request youdaoRequest = new Request.Builder()
                                .url(youdaoQuery.toString())
                                .build();
                        client.newCall(youdaoRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {

                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                JsonObject root = new JsonParser().parse(response.body().string()).getAsJsonObject();
                                // if result contains "basic" definitions, then use it
                                // else use "web" definitions.
                                if (root.get("basic") != null) {
                                    JsonArray basicDefinitions = root.get("basic").getAsJsonObject().get("explains").getAsJsonArray();
                                    for (JsonElement element : basicDefinitions) {
                                        definitions.add(element.getAsString());
                                    }
                                    // save definitions to firebase
                                    if (definitions.size() != 0) {
                                        Firebase firebase = new Firebase("https://yydictionary.firebaseio.com/youdao");
                                        Map<String, Object> update = new HashMap<>();
                                        update.put(input, definitions);
                                        firebase.updateChildren(update);
                                    }
                                } else if (root.get("web") != null) {
                                    JsonArray webDefinitionPairs = root.get("web").getAsJsonArray();
                                    for (JsonElement webDefinitionPair : webDefinitionPairs) {
                                        String key = webDefinitionPair.getAsJsonObject().get("key").getAsString();
                                        if (key.toLowerCase().equals(input)) {
                                            JsonArray webDefintionArray = webDefinitionPair.getAsJsonObject()
                                                    .get("value").getAsJsonArray();
                                            for (JsonElement webDef : webDefintionArray) {
                                                definitions.add(webDef.getAsString());
                                            }

                                        }
                                    }
                                }
                                if (definitions.size() == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            definitionTextView.setText("Sorry, but no definitions are found.");
                                        }
                                    });
                                } else {
                                    final String resultString = buildResultString(definitions);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            definitionTextView.setText(resultString);
                                        }
                                    });
                                }
                            }
                        });
                    }

                    // check if audio exists
                    // if not, download from Marriam-Webster
                    File storageFileDirectory = getFilesDir();
                    final File audioFile = new File(storageFileDirectory, input + ".wav");
                    if (!audioFile.exists()) {
                        Uri websterQuery = Uri.parse("http://www.dictionaryapi.com/api/v1/references/collegiate/xml")
                                .buildUpon()
                                .appendPath(input)
                                .appendQueryParameter("key", getString(R.string.webster_dictionary_key))
                                .build();
                        Log.d(TAG, websterQuery.toString());
                        Request websterRequest = new Request.Builder()
                                .url(websterQuery.toString())
                                .build();
                        client.newCall(websterRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {

                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                Document document = Jsoup.parse(response.body().string(), "", Parser.xmlParser());

                                // get suggestions
                                List<String> suggestions = new ArrayList<>();
                                Elements suggestionElements = document.getElementsByTag("suggestion");
                                for (Element element : suggestionElements) {
                                    suggestions.add(element.text());
                                }

                                // get audio
                                Elements wavElements = document.getElementsByTag("wav");
                                if (wavElements.size() == 0) {
                                    return;
                                } else {
                                    Element wavFirstElement = wavElements.get(0);
                                    String audioName = wavFirstElement.text();
                                    StringBuilder audioUrlBuilder = new StringBuilder();
                                    audioUrlBuilder.append("http://media.merriam-webster.com/soundc11/");
                                    if (audioName.startsWith("bix")) {
                                        audioUrlBuilder.append("bix");
                                    } else if (audioName.startsWith("gg")) {
                                        audioUrlBuilder.append("gg");
                                    } else {
                                        audioUrlBuilder.append(audioName.charAt(0));
                                    }
                                    audioUrlBuilder.append("/");
                                    audioUrlBuilder.append(audioName);

                                    // get audio file
                                    Request audioRequest = new Request.Builder()
                                            .url(audioUrlBuilder.toString())
                                            .build();
                                    client.newCall(audioRequest).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Request request, IOException e) {

                                        }

                                        @Override
                                        public void onResponse(Response response) throws IOException {
                                            byte[] bytes = response.body().bytes();
                                            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                                                fos.write(bytes);
                                            } catch (IOException e) {
                                            }
                                            playAudio(audioFile);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        playAudio(audioFile);
                    }
                }
                return false;
            }
        });
    }

    private void playAudio(File audioFile) {
        if (player != null) {
            player.stop();
        }
        player = MediaPlayer.create(getApplicationContext(), Uri.parse("file://" + audioFile.getAbsolutePath()));
        player.start();
    }

    private void retrieveYoudaoDictionary() {
        Firebase firebase = new Firebase("https://yydictionary.firebaseio.com/youdao/");
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                List<String> value = dataSnapshot.getValue(List.class);
                Word word = new Word(key, value);
                YoudaoDictionary.add(word);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private String buildResultString(List<String> definitions) {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String definition : definitions) {
            sb.append(String.format("%d. %s\n", ++count, definition));
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.play: {
                String input = inputEditText.getText().toString();
                File audioFile = new File(getFilesDir(), input + ".wav");
                if (audioFile.exists()) {
                    playAudio(audioFile);
                }
                return true;
            }
            case R.id.add: {
                String input = inputEditText.getText().toString();
                if (!input.trim().equals("")) {
                    Firebase firebase = new Firebase("http://yydictionary.firebaseio.com/builder");
                    firebase.push().setValue(input);
                }
                return true;
            }
            case R.id.builder: {
                return true;
            }
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
