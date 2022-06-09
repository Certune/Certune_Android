package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;
import com.techtown.tarsosdsp_pitchdetect.correction.PitchCorrection.PitchCorrection;
import com.techtown.tarsosdsp_pitchdetect.correction.RhythmCorrection.RhythmCorrection;
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
    ImageButton backButton;
    BottomNavigationView navigationView;

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
        backButton = findViewById(R.id.backButton);
        navigationView = findViewById(R.id.nav_view_myrecord);

        songNameTextView.setText(songName);
        singerNameTextView.setText(singerName);

        listview = (ListView) findViewById(R.id.weak_listView);

        // Adapter 생성
        adapter = new WeakSentenceListViewAdapter();
        listview.setAdapter(adapter);
        getLyricList();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyRecordActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setSelectedItemId(R.id.navigation_myRecord);
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.navigation_songList:
                        intent = new Intent(getApplicationContext(), SongListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.navigation_pitch:
                        intent = new Intent(getApplicationContext(), PitchCorrection.class);
                        startActivity(intent);
                        break;
                    case R.id.navigation_rhythm:
                        intent = new Intent(getApplicationContext(), RhythmCorrection.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
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