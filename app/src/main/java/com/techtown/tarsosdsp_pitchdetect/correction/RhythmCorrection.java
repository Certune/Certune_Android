package com.techtown.tarsosdsp_pitchdetect.correction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.MyRecordActivity;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;
import com.techtown.tarsosdsp_pitchdetect.global.CustomPitchCorrectionListDto;

public class RhythmCorrection extends AppCompatActivity {

    private ListView listView;
    private RhythmCorrectionListAdapter adapter;
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythm_correction);

        adapter = new RhythmCorrectionListAdapter();

        listView = (ListView)findViewById(R.id.Rhythm_listView);
        navigationView = findViewById(R.id.nav_view_rhythm);
        listView.setAdapter(adapter);

        adapter.addItem("2/2 박자");
        adapter.addItem("2/4 박자");
        adapter.addItem("2/8 박자");
        adapter.addItem("3/2 박자");
        adapter.addItem("3/4 박자");
        adapter.addItem("3/8 박자");
        adapter.addItem("4/2 박자");
        adapter.addItem("4/4 박자");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomPitchCorrectionListDto customPitchCorrectionListDto = (CustomPitchCorrectionListDto) parent.getItemAtPosition(position);

                String rhythm = customPitchCorrectionListDto.getOctave();
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
                    case R.id.navigation_pitch:
                        intent = new Intent(getApplicationContext(), PitchCorrection.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }
}