package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.CustomWeakSentenceListDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeakSentenceListActivity extends AppCompatActivity {

    String userEmail;
    private String songName;
    private String singerName;

    private ListView listview;
    private WeakSentenceListViewAdapter adapter;

    List<String> weakSentenceIndexList = new ArrayList<>();

    TextView songNameTextView;
    TextView singerNameTextView;

    ImageButton recordBtn;
    ImageButton listenBtn;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weak_sentence_list);

        Intent subIntent = getIntent();
        songName = subIntent.getStringExtra("songName");
        singerName = subIntent.getStringExtra("singerName");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            userEmail = user.getEmail();

        songNameTextView = findViewById(R.id.weak_songTextView);
        singerNameTextView = findViewById(R.id.weak_singerTextView);
        recordBtn = findViewById(R.id.result_playBtn);
        listenBtn = findViewById(R.id.result_listenBtn);

        songNameTextView.setText(songName);
        singerNameTextView.setText(singerName);

        listview = (ListView) findViewById(R.id.weak_listView);

        // Adapter 생성
        adapter = new WeakSentenceListViewAdapter();
        listview.setAdapter(adapter);

        getLyricList();
    }

    public void getLyricList() {
        // get weak sentence index
        Task<DocumentSnapshot> ref = database.collection("User").document(userEmail).collection("userWeakSentenceList").document(songName)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        weakSentenceIndexList = (List<String>) documentSnapshot.get("weakSentence");
                    } else {
                        Log.d(TAG, "Error getting collections: ", task.getException());
                    }
                });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // get weak sentence with index
                Task<DocumentSnapshot> querySnapshot = database.collection("Song").document(songName).get();
                querySnapshot.addOnSuccessListener(documentSnapshot -> {
                    ArrayList<HashMap<String, Object>> sentences = (ArrayList<HashMap<String, Object>>) documentSnapshot.get("sentence");
                    Log.v("weaksentence size", Integer.toString(weakSentenceIndexList.size()));
                    for (int i = 0; i < weakSentenceIndexList.size(); i++) {
                        HashMap<String, Object> lyricListMap = sentences.get(Integer.parseInt(weakSentenceIndexList.get(i)));
                        String lyric = lyricListMap.get("lyrics").toString();
                        CustomWeakSentenceListDto dto = new CustomWeakSentenceListDto();
                        dto.setSentenceText(lyric);
                        adapter.addItem(dto);
                    }
                });
            }
        }, 3000);
    }
}