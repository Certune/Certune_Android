package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.Singing.activity.SingingStandbyActivity;

import java.util.ArrayList;
import java.util.List;

public class SongListActivity extends AppCompatActivity {
    private ListView list;
    private ArrayAdapter adapter;
    private List<String> itemList = new ArrayList<>();
    String userEmail;

    // firebase db 연동
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        Intent subIntent = getIntent();
        userEmail = subIntent.getStringExtra("userEmail");

        // Song 컬렉션 내에 위치한 모든 곡 이름 가져오기
        database.collection("Song")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                itemList.add(document.getId().toString());
                                Log.d(TAG, document.getId() + " => " + document.getData().get("singer"));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                list = (ListView) findViewById(R.id.songListArea);
                adapter = new ArrayAdapter(SongListActivity.this, android.R.layout.simple_list_item_1, itemList);
                list.setAdapter(adapter);

                // 클릭 이벤트
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String songName = (String) parent.getItemAtPosition(position);
                        Intent intent = new Intent(getApplicationContext(), SingingStandbyActivity.class);
                        intent.putExtra("userEmail", userEmail);
                        intent.putExtra("songName", songName);
                        startActivity(intent);
                    }
                });
            }
        }, 4000);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}