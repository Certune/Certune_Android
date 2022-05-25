package com.techtown.tarsosdsp_pitchdetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.correction.PitchCorrection;
import com.techtown.tarsosdsp_pitchdetect.correction.RhythmCorrection;
import com.techtown.tarsosdsp_pitchdetect.global.CustomSongListDto;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.MyRecordActivity;

public class SongListActivity extends AppCompatActivity {
    private static final String TAG = "SongListActivity";

    String songName;
    String singerName;

    private ListView list;
    private SongListViewAdapter adapter;
    BottomNavigationView navigationView;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        list = (ListView) findViewById(R.id.songListArea);
        navigationView = findViewById(R.id.nav_view_songlist);
        adapter = new SongListViewAdapter();
        list.setAdapter(adapter);

        getData();

        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.navigation_myRecord:
                        intent = new Intent(getApplicationContext(), MyRecordActivity.class);
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

    public void getData() {
        // Song 컬렉션 내에 위치한 모든 곡 이름 가져오기
        database.collection("Song")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CustomSongListDto dto = new CustomSongListDto();
                                dto.setSongText(document.getId());
                                dto.setSingerText(document.getData().get("singer").toString());
                                adapter.addItem(dto);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}