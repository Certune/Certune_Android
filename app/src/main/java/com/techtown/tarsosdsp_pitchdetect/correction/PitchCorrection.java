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

        adapter.addItem("A#3 - F4");
        adapter.addItem("A3 - E4");
        adapter.addItem("B3 - F#4");
        adapter.addItem("C#3 - G#3");
        adapter.addItem("C#4 - G#4");
        adapter.addItem("C4 - G4");
        adapter.addItem("D#3 - A#3");
        adapter.addItem("D#4 - A#4");
        adapter.addItem("D3 - A3");
        adapter.addItem("D4 - A4");
        adapter.addItem("E3 - B3");
        adapter.addItem("E4 - B4");
        adapter.addItem("F#3 - C#4");
        adapter.addItem("F3 - C4");
        adapter.addItem("F4 - C5");
        adapter.addItem("G#3 - D#4");
        adapter.addItem("G3 - D4");


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