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
    List<String> songList = new ArrayList<>();
    List<String> singerList = new ArrayList<>();

    // firebase db 연동
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        // Song 컬렉션 내에 위치한 모든 곡 이름 가져오기
        database.collection("Song")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                songList.add(document.getId().toString());
                                singerList.add(document.getData().get("singer").toString());
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
                adapter = new SongListViewAdapter();
                setData();
                list = (ListView) findViewById(R.id.songListArea);
                list.setAdapter(adapter);
            }
        },3000);

    }

    private void setData() {

        for (int i = 0; i < songList.size(); i++) {
            CustomSongListDto dto = new CustomSongListDto();
            dto.setIndexText(Integer.toString(i + 1));
            dto.setSongText(songList.get(i));
            dto.setSingerText(singerList.get(i));

            adapter.addItem(dto);
        }
    }
}
