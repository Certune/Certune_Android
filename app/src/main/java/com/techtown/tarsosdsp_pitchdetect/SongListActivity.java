package com.techtown.tarsosdsp_pitchdetect;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.global.CustomSongListDto;

public class SongListActivity extends AppCompatActivity {
    private static final String TAG = "SongListActivity";

    private ListView list;
    private SongListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        list = (ListView) findViewById(R.id.songListArea); // TODO: Adapter 설정을 해주어야 하니 먼저 이렇게 view 를 얻고...

        adapter = new SongListViewAdapter(); // TODO: Adapter를 만들어서
        list.setAdapter(adapter); // TODO: 바로 먼저 적용해버리면 돼요.

        getData(); // TODO: 그리고 데이터를 가져오는 함수를 실행.
    }

    // TODO: 이 함수는 나중에 MVVM을 적용하시게 되면 repository (data source) 등으로 빼는게 좋아요. Activity 에서 할 작업은 아니라서..
    private void getData() {
        // Song 컬렉션 내에 위치한 모든 곡 이름 가져오기
        FirebaseFirestore.getInstance().collection("Song")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete()");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CustomSongListDto dto = new CustomSongListDto();
                                dto.setSongText(document.getId());
                                dto.setSingerText(document.getData().get("singer").toString());
                                adapter.addItem(dto); // TODO: 이렇게 adapter 에 추가하면 돼요.
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}