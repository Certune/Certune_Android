package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import  static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.WeakSentenceListViewAdapter;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;
import com.techtown.tarsosdsp_pitchdetect.global.CustomWeakSentenceListDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// TODO : 점수에 따라 progress bar 색상 바꾸기
// TODO : 이전 뷰에서 userEmail, singer name, song name 받아오기
public class SingingResult extends AppCompatActivity {
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    String userEmail = "nitronium007@gmail.com";

    String songName = "신호등";
    String singerName = "이무진";

    Double totalScore;
    Double noteScore;
    Double rhythmScore;

    List<String> weakSentenceIndexList = new ArrayList<>();

    // get xml instance
    ListView listview;
    WeakSentenceListViewAdapter adapter;

    Button totalScoreView;
    TextView songNameTextView;
    TextView singerNameTextView;
    TextView noteScoreTextView;
    TextView rhythmScoreTextView;

    ProgressBar noteProgressBar;
    ProgressBar rhythmProgressBar;
    Button checkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singing_result);

        checkBtn = findViewById(R.id.result_checkButton);
        checkBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SingingResult.this, SongListActivity.class);
            startActivity(intent);
            finish();
        });

        songNameTextView = findViewById(R.id.result_songTextView);
        singerNameTextView = findViewById(R.id.result_singerTextView);

        songNameTextView.setText(songName);
        singerNameTextView.setText(singerName);

        noteProgressBar = findViewById(R.id.progressBar);
        rhythmProgressBar = findViewById(R.id.progressBar2);

        listview = (ListView) findViewById(R.id.result_listView);
        adapter = new WeakSentenceListViewAdapter();
        listview.setAdapter(adapter);

        getScore();
        totalScoreView = findViewById(R.id.scoreIcon);
        noteScoreTextView = findViewById(R.id.noteScoreTextView);
        rhythmScoreTextView = findViewById(R.id.rhythmScoreTextView);

        new Handler().postDelayed(() -> {
            Log.v("score", "2");
            totalScoreView.setText(Integer.parseInt(String.valueOf(Math.round(totalScore))) +"점");
            noteScoreTextView.setText(Integer.parseInt(String.valueOf(Math.round(noteScore))) + "점");
            rhythmScoreTextView.setText(Integer.parseInt(String.valueOf(Math.round(rhythmScore))) + "점");
        }, 3000);

        getLyricList();
        setProgressColors();
    }

    private void setProgressColors() {
        // note progress
        int noteScoreProgress = (int) Math.round(noteScore);

        Drawable noteProgressDrawable;
        if (noteScoreProgress < 40)
            noteProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_bad, null);
        else if (noteScoreProgress < 70)
            noteProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_soso, null);
        else
            noteProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null);

        noteProgressBar.setProgress(noteScoreProgress);
        noteProgressBar.setProgressDrawable(noteProgressDrawable);

        // rhythm progress
        int rhythmScoreProgress = (int) Math.round(rhythmScore);

        Drawable rhythmProgressDrawable;
        if (rhythmScoreProgress < 40)
            rhythmProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_bad, null);
        else if (rhythmScoreProgress < 70)
            rhythmProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_soso, null);
        else
            rhythmProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null);

        rhythmProgressBar.setProgress(rhythmScoreProgress);
        rhythmProgressBar.setProgressDrawable(rhythmProgressDrawable);
    }

    public void getScore() {
        Log.v("score", "1");
        Task<DocumentSnapshot> ref = database.collection("User").document(userEmail).collection("userSongList").document(songName)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        totalScore = Double.valueOf(documentSnapshot.get("totalScore").toString());
                        noteScore = Double.valueOf(documentSnapshot.get("noteScore").toString());
                        rhythmScore = Double.valueOf(documentSnapshot.get("rhythmScore").toString()) ;

                        noteProgressBar.setProgress((int) Math.round(noteScore));
                        rhythmProgressBar.setProgress((int) Math.round(rhythmScore));
                    } else {
                        Log.d(TAG, "Error getting collections: ", task.getException());
                    }
                });
    }

    public void getLyricList(){
        // get weak sentence index
        Task<DocumentSnapshot> ref = database.collection("User").document(userEmail).collection("userWeakSentenceList").document(songName)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        weakSentenceIndexList = (List<String>) documentSnapshot.get("weakSentence");
                        Log.v("index of weaky", weakSentenceIndexList.get(0));
                        Log.v("index of weaky", weakSentenceIndexList.get(1));
                        Log.v("index of weaky", weakSentenceIndexList.get(2));
                    } else {
                        Log.d(TAG, "Error getting collections: ", task.getException());
                    }
                });

        new Handler().postDelayed(() -> {
            // get weak sentence with index
            Task<DocumentSnapshot> querySnapshot = database.collection("Song").document(songName).get();
            querySnapshot.addOnSuccessListener(documentSnapshot -> {
                ArrayList<HashMap<String, Object>> sentences = (ArrayList<HashMap<String, Object>>) documentSnapshot.get("sentence");
                Log.v("weaky", Integer.toString(weakSentenceIndexList.size()));
                for (int i = 0; i < weakSentenceIndexList.size(); i++) {
                    HashMap<String, Object> lyricListMap = sentences.get(Integer.parseInt(weakSentenceIndexList.get(i)));
                    String lyric = lyricListMap.get("lyrics").toString();
                    CustomWeakSentenceListDto dto = new CustomWeakSentenceListDto();
                    dto.setSentenceText(lyric);
                    adapter.addItem(dto);
                }
            });

        }, 3000);
    }
}