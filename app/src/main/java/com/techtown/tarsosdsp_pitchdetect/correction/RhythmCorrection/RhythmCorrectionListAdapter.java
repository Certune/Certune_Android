package com.techtown.tarsosdsp_pitchdetect.correction.RhythmCorrection;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techtown.tarsosdsp_pitchdetect.R;

import java.util.ArrayList;

public class RhythmCorrectionListAdapter extends BaseAdapter {

    String musicUrl;
    MediaPlayer mediaPlayer;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    private ArrayList<String> rhythmCorrectionList = new ArrayList<>();

    public RhythmCorrectionListAdapter() {
    }

    @Override
    public int getCount() {
        return rhythmCorrectionList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rhythmcorrectionlistview_item, null, false);
        }

        CustomViewHolder holder;
        holder = new CustomViewHolder();
        holder.content = (TextView) convertView.findViewById(R.id.correction_rhythmTextView);
        holder.playSongBtn = (ImageButton) convertView.findViewById(R.id.correction_playButton);

        holder.playSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAudioUrlFromFirebase(String.valueOf(position));
            }
        });

        convertView.setTag(holder);
        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        String content = rhythmCorrectionList.get(position);
        holder.content.setText(content);

        return convertView;
    }

    class CustomViewHolder {
        TextView content;
        ImageButton playSongBtn;
    }

    @Override
    public String getItem(int position) {
        return rhythmCorrectionList.get(position);
    }

    public void addItem(String rhythm) {
        rhythmCorrectionList.add(rhythm);
        notifyDataSetChanged();
    }

    private void fetchAudioUrlFromFirebase(String songIdx) {
        Log.v("song url fetch", songIdx);
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("RhythmCorrection").child(songIdx + ".mp3");

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




