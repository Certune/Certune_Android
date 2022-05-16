package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.techtown.tarsosdsp_pitchdetect.global.CustomSongListDto;


import com.techtown.tarsosdsp_pitchdetect.Singing.activity.SingingStandbyActivity;
import com.techtown.tarsosdsp_pitchdetect.global.CustomSongListDto;

import java.util.ArrayList;
import java.util.List;

public class SongListViewAdapter extends BaseAdapter {


    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<CustomSongListDto> listViewItemList = new ArrayList<CustomSongListDto>();

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    List<String> songList = new ArrayList<>();
    List<String> singerList = new ArrayList<>();

    // SongListViewAdapter 생성자
    public SongListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View 리턴
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder holder;

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, null, false);

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new CustomViewHolder();
        holder.listIndex = (TextView) convertView.findViewById(R.id.indexTextView);
        holder.songTitle = (TextView) convertView.findViewById(R.id.songTextView);
        holder.singerName = (TextView) convertView.findViewById(R.id.singerTextView);
        holder.playBtn = (ImageButton) convertView.findViewById(R.id.playingButton);

        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomSongListDto listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        holder.listIndex.setText(listViewItem.getIndexText());
        holder.songTitle.setText(listViewItem.getSongText());
        holder.singerName.setText(listViewItem.getSingerText());

        holder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("clicked", Integer.toString(position));

                Intent intent = new Intent(v.getContext(), SingingStandbyActivity.class);
                intent.putExtra("userEmail", holder.songTitle.getText());
                intent.putExtra("songName", holder.songTitle.getText());

                ((SongListActivity)v.getContext()).startActivity(intent);
            }
        });

        return convertView;
    }

    class CustomViewHolder {
        TextView listIndex;
        TextView songTitle;
        TextView singerName;
        ImageButton playBtn;
    }

    public void getData() {
        // Song 컬렉션 내에 위치한 모든 곡 이름 가져오기
        database.collection("Song")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                songList.add(document.getId());
                                singerList.add(document.getData().get("singer").toString());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public CustomSongListDto getItem(int position) {
        return listViewItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem() {
        for (int i = 0; i < songList.size(); i++) {
            CustomSongListDto item = new CustomSongListDto();

            item.setIndexText(Integer.toString(i + 1));
            item.setSongText(songList.get(i));
            item.setSingerText(singerList.get(i));

            listViewItemList.add(item);
        }
    }

}