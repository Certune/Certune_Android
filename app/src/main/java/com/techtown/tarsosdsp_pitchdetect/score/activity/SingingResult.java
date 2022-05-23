package com.techtown.tarsosdsp_pitchdetect.score.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singing_result);

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

                listview.setAdapter(adapter);
            }
        }, 2500);
    }
}