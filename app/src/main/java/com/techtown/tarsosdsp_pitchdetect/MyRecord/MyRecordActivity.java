package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;
import com.techtown.tarsosdsp_pitchdetect.correction.PitchCorrection.PitchCorrection;
import com.techtown.tarsosdsp_pitchdetect.correction.RhythmCorrection.RhythmCorrection;
import com.techtown.tarsosdsp_pitchdetect.global.CustomUserSongListDto;

import java.util.ArrayList;


public class MyRecordActivity extends AppCompatActivity {

    ListView listview;
    UserSongListViewAdapter adapter;
    ArrayList<CustomUserSongListDto> list = new ArrayList<>();
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_record);

        listview = (ListView) findViewById(R.id.myrecord_listView);
        navigationView = findViewById(R.id.nav_view_myrecord);

        adapter = new UserSongListViewAdapter();
        adapter.getUserSongList();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addItem();
                listview.setAdapter(adapter);
            }
        }, 3000);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), WeakSentenceListActivity.class);
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

//        @Override
//        public void onListBtnClick(int position) {
//            Intent intent = new Intent(getApplicationContext(), WeakSentenceListRecordActivity.class);
//            startActivity(intent);
//        }

}



