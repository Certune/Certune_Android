package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
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
    private String singerName = "이무진";

    String musicUrl;
    MediaPlayer mediaPlayer;

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
        holder.recordBtn = (ImageButton) convertView.findViewById(R.id.result_recordBtn);
        holder.playSongBtn = (ImageButton) convertView.findViewById(R.id.result_playBtn);
        holder.playUserBtn = (ImageButton) convertView.findViewById(R.id.result_listenBtn);

        holder.recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), WeakSentenceSingingActivity.class);
                intent.putExtra("singerName", singerName);
                intent.putExtra("songName", songName);
                intent.putExtra("sentenceIdx", userWeakSentenceList.get(position));
                ((WeakSentenceListActivity) v.getContext()).startActivity(intent);
            }
        });

        holder.playSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("clicked", Integer.toString(position));
                fetchAudioUrlFromFirebase(userWeakSentenceList.get(position));
            }
        });

        // TODO : 유저 목소리 들려주기
        holder.playUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchUserUrlFromFirebase(userWeakSentenceList.get(position));
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
        ImageButton playSongBtn;
        ImageButton playUserBtn;
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

    private void fetchUserUrlFromFirebase(String songIdx) {
        Log.v("user url fetch", songIdx);
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("User").child(userEmail).child("songs").child(songName).child(songIdx + ".mp3");

        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        musicUrl = uri.toString();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        try {
                            mediaPlayer.setDataSource(musicUrl);
                            mediaPlayer.prepareAsync();
                        } catch (Exception e) {
                            Log.e("FETCH AUDIO", e.getMessage());
                        }
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                Log.e("user mediaplayer 시작", "mediaplayerstart");
                                mediaPlayer.start();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("음악 백그라운드 재생 실패", e.getMessage());
                    }
                });
    }

    private void fetchAudioUrlFromFirebase(String songIdx) {
        Log.v("song url fetch", songIdx);
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("songs").child("신호등").child(songIdx + ".mp3");

        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        musicUrl = uri.toString();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        try {
                            mediaPlayer.setDataSource(musicUrl);
                            mediaPlayer.prepareAsync();
                        } catch (Exception e) {
                            Log.e("FETCH AUDIO", e.getMessage());
                        }
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                Log.e("song mediaplayer 시작", "mediaplayerstart");
                                mediaPlayer.start();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("음악 백그라운드 재생 실패", e.getMessage());
                    }
                });
    }
}