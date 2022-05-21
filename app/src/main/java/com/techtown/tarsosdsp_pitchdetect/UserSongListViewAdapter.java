package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.global.CustomUserSongListDto;

import java.util.ArrayList;
import java.util.List;

public class UserSongListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    ArrayList<CustomUserSongListDto> userSongLists = new ArrayList<>();
    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    private String user = "nitronium007@gmail.com";

    List<String> songList = new ArrayList<>();
    List<String> singerList = new ArrayList<>();
    List<String> scoreList = new ArrayList<>();
    List<String> noteScoreList = new ArrayList<>();
    List<String> rhythmScoreList = new ArrayList<>();

    public UserSongListViewAdapter(){
        
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return userSongLists.size() ;
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.myrecordlistview_item, null, false);

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new CustomViewHolder();
        holder.songTitle = (TextView) convertView.findViewById(R.id.songTextView);
        holder.singerName = (TextView) convertView.findViewById(R.id.singerTextView);
        holder.totalScore = (TextView) convertView.findViewById(R.id.scoreTextView);
//        holder.noteScore = (TextView) convertView.findViewById(R.id.noteScoreTextView);
//        holder.rhythmScore = (TextView)convertView.findViewById(R.id.rhythmScoreTextView);

        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomUserSongListDto listViewItem = userSongLists.get(position);

        holder.songTitle.setText(listViewItem.getSongText());
        holder.singerName.setText(listViewItem.getSingerText());
        holder.totalScore.setText(listViewItem.getTotalScoreText());
        holder.noteScore.setText(listViewItem.getNoteScoreText());
        holder.rhythmScore.setText(listViewItem.getRhythmScoreText());


        return convertView;
    }

    class CustomViewHolder {
        TextView songTitle;
        TextView singerName;
        TextView totalScore;
        TextView noteScore;
        TextView rhythmScore;
    }

    public void getUserSongList() {
        Log.v("*****", "getting item");
        CollectionReference ref = database.collection("User").document(user).collection("userSongList");
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String song = document.getId();
                        String singer = document.getData().get("singerName").toString();
                        String totalScore = document.getData().get("totalScore").toString();
                        String noteScore = document.getData().get("noteScore").toString();
                        String rhythmScore = document.getData().get("rhythmScore").toString();
                        Log.v("음정점수 ",noteScore);
                        Log.v("박자점수",rhythmScore);
                        songList.add(song);
                        singerList.add(singer);
                        scoreList.add(totalScore);
                        noteScoreList.add(noteScore);
                        rhythmScoreList.add(rhythmScore);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public CustomUserSongListDto getItem(int position) {
        return userSongLists.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem() {
        Log.v("*****", "adding item");
        for (int i = 0; i < songList.size(); i++) {
            CustomUserSongListDto item = CustomUserSongListDto
                    .builder()
                    .songText(songList.get(i))
                    .singerText(singerList.get(i))
                    .totalScoreText(scoreList.get(i))
                    .noteScoreText(noteScoreList.get(i))
                    .rhythmScoreText(rhythmScoreList.get(i))
                    .build();

            userSongLists.add(item);
        }
    }
}
