package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techtown.tarsosdsp_pitchdetect.Singing.activity.SingingActivity;
import com.techtown.tarsosdsp_pitchdetect.global.CustomWeakSentenceListDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeakSentenceListViewAdapter extends BaseAdapter {

    String musicUrl;
    MediaPlayer mediaPlayer;

    private SingingActivity singingActivity;
    private ArrayList<CustomWeakSentenceListDto> weakSentenceLists = new ArrayList<CustomWeakSentenceListDto>();
    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    // TODO : intent에서 값 받아오기
    private String user = "nitronium007@gmail.com";
    private String song = "신호등";

    List<String> LyricLists = new ArrayList<>();
    List<String> weakSentenceList = new ArrayList<>();


    //WeakSentenceListViewAdapter 생성자
    public WeakSentenceListViewAdapter() {
        fetchAudioUrlFromFirebase();
    }

    //Adapter에 사용되는 데이터 개수 리턴
    @Override
    public int getCount() {
        return weakSentenceLists.size();
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder holder;

        final int pos = position;
        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.weaksentencelistview_item, null, false);
        }
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new CustomViewHolder();
        holder.sentence = (TextView) convertView.findViewById(R.id.sentenceTextView);
        holder.recordBtn = (ImageButton) convertView.findViewById(R.id.result_playBtn);
        holder.playBtn = (ImageButton) convertView.findViewById(R.id.result_listenBtn);
        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomWeakSentenceListDto listViewItem = weakSentenceLists.get(position);
        holder.sentence.setText(listViewItem.getSentenceText());


        holder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("버튼클릭", "얍");
                mediaPlayer.start();
                Log.v("버튼클릭완", "얍");
            }
        });
        return convertView;
    }


    class CustomViewHolder {
        TextView sentence;
        ImageButton recordBtn;
        ImageButton playBtn;
    }

    public List getWeakSentenceListInfo() {
        Task<DocumentSnapshot> ref = database.collection("User").document(user).collection("userWeakSentenceList").document(song)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        weakSentenceList = (List<String>) documentSnapshot.get("weakSentence");
                    } else {
                        Log.d(TAG, "Error getting collections: ", task.getException());
                    }
                });
        return weakSentenceList;
    }

    public List<String> getLyricList() {
        weakSentenceList = getWeakSentenceListInfo();
        Task<DocumentSnapshot> querySnapshot = database.collection("Song").document(song).get();
        //get()을 통해서 해당 문서의 정보를 가져온다.
        querySnapshot.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //가사 가져오기
                ArrayList<HashMap<String, Object>> sentences = (ArrayList<HashMap<String, Object>>) documentSnapshot.get("sentence");
                for (int i = 0; i < weakSentenceList.size(); i++) {
                    HashMap<String, Object> LyricListMap = sentences.get(Integer.parseInt(weakSentenceList.get(i)));
                    String Lyrics = LyricListMap.get("lyrics").toString();
                    LyricLists.add(Lyrics);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("ERROR", "");
                    }

                });
        return LyricLists;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public CustomWeakSentenceListDto getItem(int position) {
        return weakSentenceLists.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem() {
        for (int i = 0; i < LyricLists.size(); i++) {
            CustomWeakSentenceListDto item = CustomWeakSentenceListDto
                    .builder()
                    .sentenceText(LyricLists.get(i))
                    .build();

            weakSentenceLists.add(item);
        }
    }

    private void fetchAudioUrlFromFirebase() {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("songs").child("신호등").child("0.mp3");

        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        musicUrl = uri.toString();
                        createMediaPlayer();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("음악 백그라운드 재생 실패", e.getMessage());
                    }
                });
    }

    public void createMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(musicUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.v("준비완", "짠");
                }
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("MEDIAPLAYER", e.getMessage());
        }
    }

}