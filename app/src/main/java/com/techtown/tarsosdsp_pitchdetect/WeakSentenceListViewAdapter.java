package com.techtown.tarsosdsp_pitchdetect.MyRecord;

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
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.CustomWeakSentenceListDto;

import java.util.ArrayList;
import java.util.List;

public class WeakSentenceListViewAdapter extends BaseAdapter {
    // TODO : intent에서 값 받아오기
    private String userEmail = "nitronium007@gmail.com";
    private String songName = "신호등";

    String musicUrl;
    MediaPlayer mediaPlayer;
    List<MediaPlayer> mediaPlayerList = new ArrayList<>();


    private ArrayList<CustomWeakSentenceListDto> weakSentenceList = new ArrayList<CustomWeakSentenceListDto>();
    private ArrayList<String> userWeakSentenceList = new ArrayList<>();

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    public WeakSentenceListViewAdapter() {
        getWeakSentenceList();
    }

    private void getWeakSentenceList() {
        Task<DocumentSnapshot> ref = database.collection("User").document(userEmail).collection("userWeakSentenceList").document(songName)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        userWeakSentenceList = (ArrayList<String>) documentSnapshot.get("weakSentence");
                        for (int i=0; i<userWeakSentenceList.size(); i++){
                            fetchAudioUrlFromFirebase(userWeakSentenceList.get(i));
                        }

                    } else {
                        Log.d(TAG, "Error getting collections: ", task.getException());
                    }
                });
    }

    @Override
    public int getCount() {
        return weakSentenceList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.weaksentencelistview_item, null, false);
        }

        CustomViewHolder holder;
        holder = new CustomViewHolder();

        holder.sentence = (TextView) convertView.findViewById(R.id.sentenceTextView);
        holder.recordBtn = (ImageButton) convertView.findViewById(R.id.result_playBtn);
        holder.playBtn = (ImageButton) convertView.findViewById(R.id.result_listenBtn);
        holder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("clicked", Integer.toString(position));
                MediaPlayer mediaPlayer = mediaPlayerList.get(position);
                mediaPlayer.start();
            }
        });
        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomWeakSentenceListDto listViewItem = weakSentenceList.get(position);
        holder.sentence.setText(listViewItem.getSentenceText());

        return convertView;
    }

    class CustomViewHolder {
        TextView sentence;
        ImageButton recordBtn;
        ImageButton playBtn;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public CustomWeakSentenceListDto getItem(int position) {
        return weakSentenceList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem(CustomWeakSentenceListDto dto) {
        weakSentenceList.add(dto);
        notifyDataSetChanged();
    }

    private void fetchAudioUrlFromFirebase(String songIdx) {
        Log.v("fetch", songIdx);
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("songs").child("신호등").child(songIdx+".mp3");

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
                        Log.e("음악 백그라운드 재생 실패", e.getMessage());
                    }
                });
    }

    public void createMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(musicUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayerList.add(mediaPlayer);
        } catch (Exception e) {
            Log.e("MEDIAPLAYER", e.getMessage());
        }
    }
}