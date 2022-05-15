package com.techtown.tarsosdsp_pitchdetect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

public class SongListActivity extends AppCompatActivity {
    private ListView list;
    private SongListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        adapter = new SongListViewAdapter();
        adapter.getData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addItem();

                list = (ListView) findViewById(R.id.songListArea);
                list.setAdapter(adapter);
            }
        },2500);
    }

}