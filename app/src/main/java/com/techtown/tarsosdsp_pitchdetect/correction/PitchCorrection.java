package com.techtown.tarsosdsp_pitchdetect.correction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.CustomPitchCorrectionListDto;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.MyRecordActivity;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;

public class PitchCorrection extends AppCompatActivity {

    private ListView listView;
    private PitchCorrectionListAdapter adapter;
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_correction);

        adapter = new PitchCorrectionListAdapter();

        listView = (ListView)findViewById(R.id.Pitch_listView);
        navigationView = findViewById(R.id.nav_view_pitch);

        listView.setAdapter(adapter);

        adapter.addItem("C4 - C5");
        adapter.addItem("D4 - D5");
        adapter.addItem("E4 - E5");
        adapter.addItem("F4 - F5");
        adapter.addItem("G4 - G5");
        adapter.addItem("A4 - A5");
        adapter.addItem("B4 - B5");
        adapter.addItem("C5 - C6");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomPitchCorrectionListDto customPitchCorrectionListDto = (CustomPitchCorrectionListDto) parent.getItemAtPosition(position);

                String noteRange = customPitchCorrectionListDto.getOctave();
            }
        });

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
}