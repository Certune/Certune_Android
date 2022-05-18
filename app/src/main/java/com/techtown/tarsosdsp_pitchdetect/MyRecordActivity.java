package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.domain.CustomUserSongListDto;

import java.util.ArrayList;
import java.util.List;

public class MyRecordActivity extends AppCompatActivity {

    private ListView listview;
    private UserSongListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_record);

        adapter = new UserSongListViewAdapter();
        adapter.getUserSongList();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addItem();

                listview = (ListView) findViewById(R.id.listView);
                listview.setAdapter(adapter);
            }
        }, 2500);


//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView parent, View v, int position, long id) {
//                // TODO : 버튼 추가
//            }
//
//        });
    }

//        @Override
//        public void onListBtnClick(int position) {
//            Intent intent = new Intent(getApplicationContext(), WeakSentenceListRecordActivity.class);
//            startActivity(intent);
//        }


}

