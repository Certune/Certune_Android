package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.CustomUserSongListDto;

import java.util.ArrayList;
import java.util.List;

public class UserSongListViewAdapter extends BaseAdapter {

    // 버튼 클릭 이벤트를 위한 Listener 인터페이스 정의
    public interface ListBtnClickListener{
        void onListBtnClick(int position);
    }

    // 생성자로부터 전달된 ListBtnClickListener 저장
    private ListBtnClickListener listBtnClickListener ;

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    ArrayList<CustomUserSongListDto> userSongLists = new ArrayList<>();
    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    private String userEmail;

    List<String> songList = new ArrayList<>();
    List<String> singerList = new ArrayList<>();
    List<String> scoreList = new ArrayList<>();

    public UserSongListViewAdapter(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            userEmail = user.getEmail();
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

        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.myrecordlistview_item, null, false);

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new CustomViewHolder();
        holder.songTitle = (TextView) convertView.findViewById(R.id.songTextView);
        holder.singerName = (TextView) convertView.findViewById(R.id.singerTextView);
        holder.totalScore = (TextView) convertView.findViewById(R.id.scoreTextView);
        holder.scoreBtn = (ImageButton) convertView.findViewById(R.id.scoreButton_image);
        convertView.setTag(holder);


        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomUserSongListDto listViewItem = userSongLists.get(position);

        holder.songTitle.setText(listViewItem.getSongText());
        holder.singerName.setText(listViewItem.getSingerText());
        holder.totalScore.setText(listViewItem.getTotalScoreText());
        int score = Integer.parseInt(scoreList.get(position));
      
        if(score<40){
            holder.scoreBtn.setBackgroundResource(R.drawable.myrecord_score3_background);
        }else if(score<70){
            holder.scoreBtn.setBackgroundResource(R.drawable.myrecord_score2_background);
        } else {
            holder.scoreBtn.setBackgroundResource(R.drawable.myrecord_score1_background);
        }
        holder.scoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.songTitle.getText().equals("신호등")) {
                    Intent intent = new Intent(v.getContext(), WeakSentenceListActivity.class);
                    intent.putExtra("songName", holder.songTitle.getText());
                    intent.putExtra("singerName", holder.singerName.getText());
                    ((MyRecordActivity) v.getContext()).startActivity(intent);
                }
                else
                    Toast.makeText(v.getContext(), "현재는 신호등만 제공됩니다.", Toast.LENGTH_LONG).show();
            }
        });

        return convertView;
    }

    class CustomViewHolder {
        TextView songTitle;
        TextView singerName;
        TextView totalScore;
        ImageButton scoreBtn;
    }

    public void getUserSongList() {
        Log.v("*****", "getting item");
        CollectionReference ref = database.collection("User").document(userEmail).collection("userSongList");
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String song =  document.getId();
                        String singer = document.getData().get("singerName").toString();
                        String totalScore = document.getData().get("totalScore").toString();

                        songList.add(song);
                        singerList.add(singer);
                        scoreList.add(String.valueOf((int)Double.parseDouble(totalScore)));
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
                    .build();

            userSongLists.add(item);
        }
    }
}