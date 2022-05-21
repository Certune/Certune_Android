package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SingingTableActivity extends AppCompatActivity {

    private String songName;
    private String songLowKey;
    private String songHighKey;
    private Boolean isShifting;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_singing_table);

        findSongInfo();
    }

    private void findSongInfo() {
        DocumentReference docRef = database.collection("Song").document(songName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {

                        // 가사 정보 가져오기
                        List list = (List) Objects.requireNonNull(document.getData()).get("sentence");
                        HashMap<String, String> firstMap = (HashMap) list.get(0);
                        String firstLyrics = firstMap.get("lyrics");
                        HashMap<String, String> secondMap = (HashMap) list.get(1);
                        String secondLyrics = secondMap.get("lyrics");

                        // 노래 최저, 최고음 가져오기
                        songLowKey = (String) document.getData().get("lowKey");
                        songHighKey = (String) document.getData().get("highKey");
                    } catch (Exception e) {
                        Log.e("Song 정보 import", "노래 정보 로딩에 실패했습니다");
                    }
                }
            }
        });
    }
}