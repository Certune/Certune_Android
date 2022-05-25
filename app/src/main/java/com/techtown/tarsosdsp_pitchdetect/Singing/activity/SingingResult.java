package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;
import com.techtown.tarsosdsp_pitchdetect.WeakSentenceListViewAdapter;

// TODO : 점수에 따라 progress bar 색상 바꾸기
public class SingingResult extends AppCompatActivity {

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    String song = "신호등";
    String singerName = "이무진";
    double noteScore;
    double rhythmScore;

    // get xml instance
    ListView listview;
    WeakSentenceListViewAdapter adapter;
    TextView songNameTextView;
    TextView singerNameTextView;
    TextView noteScoreTextView;
    TextView rhythmScoreTextView;
    ProgressBar noteProgressBar;
    ProgressBar rhythmProgressBar;
    Button recordBtn;
    Button checkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singing_result);

        checkBtn = findViewById(R.id.result_checkButton);

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingingResult.this, SongListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        songNameTextView = findViewById(R.id.result_songTextView);
        singerNameTextView = findViewById(R.id.result_singerTextView);
//        noteScoreTextView = findViewById(R.id.noteScoreTextView);
//        rhythmScoreTextView = findViewById(R.id.rhythmScoreTextView);

        noteProgressBar = findViewById(R.id.progressBar);
        rhythmProgressBar = findViewById(R.id.progressBar2);

        listview = (ListView) findViewById(R.id.result_listView);
        adapter = new WeakSentenceListViewAdapter();
        adapter.getLyricList();

        // TODO : 이전 뷰에서 singer name, song name 받아오기
        songNameTextView.setText(song);
        singerNameTextView.setText(singerName);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addItem();

                listview = (ListView) findViewById(R.id.result_listView);
                listview.setAdapter(adapter);
            }
        }, 3500);
    }
}