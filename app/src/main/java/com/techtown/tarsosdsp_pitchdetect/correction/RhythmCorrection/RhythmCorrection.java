package com.techtown.tarsosdsp_pitchdetect.correction.RhythmCorrection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.MyRecordActivity;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;
import com.techtown.tarsosdsp_pitchdetect.correction.PitchCorrection.PitchCorrection;

import java.util.ArrayList;
import java.util.HashMap;

public class RhythmCorrection extends AppCompatActivity {

    private ListView listView;
    private RhythmCorrectionListAdapter adapter;
    BottomNavigationView navigationView;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythm_correction);

        adapter = new RhythmCorrectionListAdapter();

        listView = (ListView)findViewById(R.id.Rhythm_listView);
        navigationView = findViewById(R.id.nav_view_rhythm);

        listView.setAdapter(adapter);
        getRhythmList();

        navigationView.setSelectedItemId(R.id.navigation_rhythm);
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
                    case R.id.navigation_pitch:
                        intent = new Intent(getApplicationContext(), PitchCorrection.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    public void getRhythmList() {
        database.collection("Correction").document("RhythmCorrection").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                try {
                    ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) document.getData().get("rhythms");

                    for (int i=0; i<list.size(); i++){
                        HashMap<String, Object> sentencemap = (HashMap<String, Object>) list.get(i);
                        String rhythmInfo = (String) sentencemap.get("notes");
                        adapter.addItem(rhythmInfo);
                    }
                } catch (Exception e) {
                    Log.e("getRhythmList", "해당 소절의 정보를 가져올 수 없습니다.");
                }
            }
        });

    }
}