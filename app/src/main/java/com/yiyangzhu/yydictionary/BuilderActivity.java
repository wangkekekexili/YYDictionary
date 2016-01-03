package com.yiyangzhu.yydictionary;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.joda.time.LocalDate;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * This is the activity for vocabulary builder.
 *
 * @author kewang
 * Created by kewang on 1/1/16.
 */
public class BuilderActivity extends AppCompatActivity {

    @Bind(R.id.show_container)
    RelativeLayout showContainer;

    @Bind(R.id.show)
    TextView show;

    @Bind(R.id.no)
    ImageButton no;

    @Bind(R.id.yes)
    ImageButton yes;

    private MediaPlayer player;

    private boolean flipped = false;

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_builder);

        ButterKnife.bind(this);

        final PriorityQueue<VocabularyBuilderItem> words = new PriorityQueue<>(100, new Comparator<VocabularyBuilderItem>() {
            @Override
            public int compare(VocabularyBuilderItem lhs, VocabularyBuilderItem rhs) {
                if (lhs.getNextDate().isBefore(rhs.getNextDate())) {
                    return -1;
                } else if (lhs.getNextDate().isAfter(rhs.getNextDate())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        Firebase firebase = new Firebase("https://yydictionary.firebaseio.com/builder/");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Map<String, Object>> value = dataSnapshot.getValue(Map.class);
                for (Map.Entry<String, Map<String, Object>> entry : value.entrySet()) {
                    VocabularyBuilderItem item = new VocabularyBuilderItem();
                    item.setWord((String)entry.getValue().get("word"));
                    item.setNextDate((String)entry.getValue().get("nextDate"));
                    item.setIteration((int)entry.getValue().get("iteration"));
                    words.add(item);
                }

                if (!words.isEmpty()) {
                    VocabularyBuilderItem front = words.peek();
                    if (front.getNextDate().isAfter(LocalDate.now())) {
                        showEnd();
                    } else {
                        show.setText(front.getWord());
                        show.setGravity(Gravity.CENTER_HORIZONTAL);
                        player = MediaPlayer.create(BuilderActivity.this,
                                Uri.parse("file://" + new File(getFilesDir(), front.getWord() + ".wav").toString()));
                        if (player != null) {
                            player.start();
                        }
                    }
                } else {
                    showEnd();
                }

                showContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!flipped) {
                            flipped = true;
                            Word word = YoudaoDictionary.get(show.getText().toString());
                            if (word != null) {
                                show.setText(word.toString());
                                show.setGravity(Gravity.NO_GRAVITY);
                            }
                        }
                    }
                });

                show.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!flipped) {
                            flipped = true;
                            Word word = YoudaoDictionary.get(show.getText().toString());
                            if (word != null) {
                                show.setText(word.toString());
                                show.setGravity(Gravity.NO_GRAVITY);
                            }
                        }
                    }
                });

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VocabularyBuilderItem front = words.poll();
                        front.nextIteration();
                        // update firebase
                        Firebase firebase1 = new Firebase("https://yydictionary.firebaseio.com/builder");
                        Map<String, Object> update = new HashMap<>();
                        Map<String, Object> update1 = new HashMap<>();
                        update1.put("word", front.getWord());
                        update1.put("nextDate", front.getNextDate().toString());
                        update1.put("iteration", front.getIteration());
                        update.put(front.getWord(), update1);
                        firebase1.updateChildren(update);
                        // get next word
                        flipped = false;
                        if (!words.isEmpty()) {
                            front = words.peek();
                            if (front.getNextDate().isAfter(LocalDate.now())) {
                                showEnd();
                            } else {
                                show.setText(front.getWord());
                                show.setGravity(Gravity.CENTER_HORIZONTAL);
                                player = MediaPlayer.create(BuilderActivity.this,
                                        Uri.parse("file://" + new File(getFilesDir(), front.getWord() + ".wav").toString()));
                                if (player != null) {
                                    player.start();
                                }
                            }
                        } else {
                            showEnd();
                        }
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VocabularyBuilderItem front = words.poll();
                        front.setIteration(1);
                        front.setNextDate(LocalDate.now());
                        words.add(front);
                        // update firebase
                        Firebase firebase1 = new Firebase("https://yydictionary.firebaseio.com/builder");
                        Map<String, Object> update = new HashMap<>();
                        Map<String, Object> update1 = new HashMap<>();
                        update1.put("word", front.getWord());
                        update1.put("nextDate", front.getNextDate().toString());
                        update1.put("iteration", front.getIteration());
                        update.put(front.getWord(), update1);
                        firebase1.updateChildren(update);
                        // get next word
                        flipped = false;
                        if (!words.isEmpty()) {
                            front = words.peek();
                            if (front.getNextDate().isAfter(LocalDate.now())) {
                                showEnd();
                            } else {
                                show.setText(front.getWord());
                                show.setGravity(Gravity.CENTER_HORIZONTAL);
                                player = MediaPlayer.create(BuilderActivity.this,
                                        Uri.parse("file://" + new File(getFilesDir(), front.getWord() + ".wav").toString()));
                                if (player != null) {
                                    player.start();
                                }
                            }
                        } else {
                            showEnd();
                        }
                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    public void showEnd() {
        show.setText(getString(R.string.finished));
        yes.setVisibility(View.INVISIBLE);
        no.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
