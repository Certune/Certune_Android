package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.domain.CustomSongListDto;

import java.util.ArrayList;
import java.util.List;

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