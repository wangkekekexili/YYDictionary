package com.yiyangzhu.yydictionary;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.input) EditText inputEditText;
    @Bind(R.id.definition) TextView definitionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Firebase.setAndroidContext(getApplicationContext());

        inputEditText.setCursorVisible(false);
        inputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEditText.setText("");
                definitionTextView.setText("");
                inputEditText.setCursorVisible(true);
            }
        });

        final List<String> definitions = new ArrayList<String>();
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
                    Uri queryUri = Uri.parse("http://fanyi.youdao.com/openapi.do?type=data&doctype=json&version=1.1")
                            .buildUpon()
                            .appendQueryParameter("keyfrom", getString(R.string.youdao_api_keyfrom))
                            .appendQueryParameter("key", getString(R.string.youdao_api_key))
                            .appendQueryParameter("q", input)
                            .build();
                    Log.d(TAG, queryUri.toString());
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(queryUri.toString())
                            .build();
                    client.newCall(request).enqueue(new Callback() {
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
                            }
                            if (definitions.size() == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        definitionTextView.setText("Sorry, but no definitions are found.");
                                    }
                                });
                            } else {
                                // build result string
                                final StringBuilder sb = new StringBuilder();
                                int count = 0;
                                for (String definition : definitions) {
                                    sb.append(String.format("%d. %s\n", ++count, definition));
                                }
                                sb.deleteCharAt(sb.length() - 1);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        definitionTextView.setText(sb.toString());
                                    }
                                });

                                // save definitions to firebase
                                Firebase firebase = new Firebase("https://yydictionary.firebaseio.com/youdao");
                                Map<String, Object> update = new HashMap<>();
                                update.put(input, definitions);
                                firebase.updateChildren(update);
                            }
                        }
                    });
                }
                return false;
            }
        });
    }
}
