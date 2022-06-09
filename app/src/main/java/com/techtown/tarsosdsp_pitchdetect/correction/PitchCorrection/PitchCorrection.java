package com.techtown.tarsosdsp_pitchdetect.correction.PitchCorrection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.correction.RhythmCorrection.RhythmCorrection;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.MyRecordActivity;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class PitchCorrection extends AppCompatActivity {

    private ListView listView;
    private PitchCorrectionListAdapter adapter;
    BottomNavigationView navigationView;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_correction);

        adapter = new PitchCorrectionListAdapter();

        listView = (ListView)findViewById(R.id.Pitch_listView);
        navigationView = findViewById(R.id.nav_view_pitch);

        listView.setAdapter(adapter);
        getPitchList();

        navigationView.setSelectedItemId(R.id.navigation_pitch);
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.navigation_songList:
                        intent = new Intent(getApplicationContext(), SongListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.navigation_myRecord:
                        intent = new Intent(getApplicationContext(), MyRecordActivity.class);
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

    public void getPitchList() {
        database.collection("Correction").document("PitchCorrection").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                try {
                    ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) document.getData().get("octaves");

                    for (int i=0; i<list.size(); i++){
                        HashMap<String, Object> sentencemap = (HashMap<String, Object>) list.get(i);
                        String octaveInfo = (String) sentencemap.get("octave_info");
                        adapter.addItem(octaveInfo);
                    }
                } catch (Exception e) {
                    Log.e("getPitchCorrectionList", "해당 소절의 정보를 가져올 수 없습니다.");
                }
            }
        });

    }
}