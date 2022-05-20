package com.techtown.tarsosdsp_pitchdetect.score.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.UserSongListViewAdapter;
import com.techtown.tarsosdsp_pitchdetect.WeakSentenceListViewAdapter;

public class SingingResult extends AppCompatActivity {
    private ListView listview;
    private WeakSentenceListViewAdapter adapter;
    private UserSongListViewAdapter adapter2;
    private String song = "신호등";
    private String singerName;
    private double noteScore ;
    private double rhythmScore;


    TextView songNameTextView;
    TextView singerNameTextView;
    TextView noteScoreTextView;
    TextView rhythmScoreTextView;
    ProgressBar noteProgressBar;
    ProgressBar rhythmProgressBar;

    Button recordBtn;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singing_result);

        songNameTextView = findViewById(R.id.songTextView);
        singerNameTextView = findViewById(R.id.singerTextView);
//        noteScoreTextView = findViewById(R.id.noteScoreTextView);
//        rhythmScoreTextView = findViewById(R.id.rhythmScoreTextView);
//
        noteProgressBar = findViewById(R.id.progressBar);
        rhythmProgressBar = findViewById(R.id.progressBar2);



        // Adapter 생성
        adapter2 = new UserSongListViewAdapter();
        adapter2.getUserSongList();
        adapter = new WeakSentenceListViewAdapter();
        adapter.getLyricList();
        findSongInfo();


        songNameTextView.setText(song);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addItem();
                adapter2.addItem();

                listview = (ListView) findViewById(R.id.listView);
                listview.setAdapter(adapter);

            }
        }, 2500);

        /*
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WeakSentenceListRecordActivity.class);
                intent.putExtra("songName", songName);
                intent.putExtra("singerName", singerName);
                startActivity(intent);

            }
        });
   */
    }

    public void findSongInfo() {
        DocumentReference docRef = database.collection("Song").document(song);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        singerName = document.getData().get("singer").toString();
                        singerNameTextView.setText(singerName);
                    } catch (Exception e) {
                        Log.e("Song 정보 import", "노래 정보 로딩에 실패했습니다");
                    }
                }
            }
        });
    }



}