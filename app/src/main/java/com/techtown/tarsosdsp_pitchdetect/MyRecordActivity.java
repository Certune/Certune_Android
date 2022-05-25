package com.techtown.tarsosdsp_pitchdetect;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class MyRecordActivity extends AppCompatActivity {

    ListView listview;
    UserSongListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_record);

        listview = (ListView) findViewById(R.id.listView2);

        adapter = new UserSongListViewAdapter();
        adapter.getUserSongList();



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addItem();
                listview.setAdapter(adapter);

            }
        }, 3500);


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



