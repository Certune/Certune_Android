package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class WeakSentenceListActivity extends AppCompatActivity {
    private ListView list;

    //취약소절리스트 데이터 담을 리스트
    List <String> itemList = new ArrayList<>();


    // firebase db 연동
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weak_sentence_list);

        // User 컬렉션 > userName > userWeakSentenceList > "신호등" > weakSentence ArrayList에 있는 모든 요소 가져오기
        CollectionReference ref = database.collection("User").document("user@naver.com").collection("userWeakSentenceList");
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    itemList = (List<String>) document.get("weakSentence");
                    for (String s : itemList) {
                        Log.d(TAG, s);
                    }
                }
            } else {
                Log.d(TAG, "Error getting collections: ", task.getException());
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                list = (ListView) findViewById(R.id.listView);
                ArrayAdapter adapter = new ArrayAdapter(WeakSentenceListActivity.this, android.R.layout.simple_list_item_1,itemList);
                list.setAdapter(adapter);
            }
        },4000);

    }

    @Override
    protected void onStart() {



        super.onStart();
    }
}
