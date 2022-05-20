package com.techtown.tarsosdsp_pitchdetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class WeakSentenceListActivity extends AppCompatActivity {
    private ListView listview;
    private WeakSentenceListViewAdapter adapter;
    private String song = "신호등";
    private String singerName;

    TextView songNameTextView;
    TextView singerNameTextView;

    Button recordBtn;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weak_sentence_list);

        // TODO : intent 추가해서 값 받아오기

        songNameTextView = findViewById(R.id.myRecord_songTextView);
        singerNameTextView = findViewById(R.id.myRecord_singerTextView);


        // Adapter 생성
        adapter = new WeakSentenceListViewAdapter();
        adapter.getLyricList();
        findSongInfo();

        songNameTextView.setText(song);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addItem();

                listview = (ListView) findViewById(R.id.listView);
                listview.setAdapter(adapter);
            }
        }, 2500);

/*
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WeakSentenceListRecordActivity.class);
                intent.putExtra("songName", songName);
                intent.putExtra("singerName", singerName);
                startActivity(intent);

            }
        });
   */
    }

    public void findSongInfo() {
        DocumentReference docRef = database.collection("Song").document(song);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        singerName = document.getData().get("singer").toString();
                        singerNameTextView.setText(singerName);
                    } catch (Exception e) {
                        Log.e("Song 정보 import", "노래 정보 로딩에 실패했습니다");
                    }
                }
            }
        });
    }

}
