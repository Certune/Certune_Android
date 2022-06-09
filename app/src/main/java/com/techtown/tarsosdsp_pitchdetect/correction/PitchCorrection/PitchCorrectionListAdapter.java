package com.techtown.tarsosdsp_pitchdetect.correction.PitchCorrection;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techtown.tarsosdsp_pitchdetect.R;

import java.util.ArrayList;

public class PitchCorrectionListAdapter extends BaseAdapter  {

    String userEmail;
    String musicUrl;
    MediaPlayer mediaPlayer;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    private ArrayList<String> pitchCorrectionList = new ArrayList<>();

    public PitchCorrectionListAdapter() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            userEmail = user.getEmail();
    }

    @Override
    public int getCount() {
        return pitchCorrectionList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pitchcorrectionlistview_item, null, false);
        }

        CustomViewHolder holder;
        holder = new CustomViewHolder();
        holder.content = (TextView) convertView.findViewById(R.id.correction_pitchTextView);
        holder.recordBtn = (ImageButton) convertView.findViewById(R.id.recordButton);
        holder.playSongBtn = (ImageButton) convertView.findViewById(R.id.correction_playButton);
        holder.playUserBtn = (ImageButton) convertView.findViewById(R.id.correction_listenButton);

        holder.recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PitchCorrectionSingingActivity.class);
                intent.putExtra("sentenceIdx", String.valueOf(position));
                ((PitchCorrection)v.getContext()).startActivity(intent);
            }
        });

        holder.playSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAudioUrlFromFirebase(String.valueOf(position));
            }
        });

        holder.playUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isUserUrlExist = fetchUserUrlFromFirebase(String.valueOf(position));
                if (!isUserUrlExist)
                    Toast.makeText(v.getContext(), "연습 기록이 없습니다.", Toast.LENGTH_LONG).show();
            }
        });

        convertView.setTag(holder);
        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        String content = pitchCorrectionList.get(position);
        holder.content.setText(content);

        return convertView;
    }

    class CustomViewHolder {
        TextView content;
        ImageButton playSongBtn;
        ImageButton playUserBtn;
        ImageButton recordBtn;
    }

    @Override
    public String getItem(int position){
        return pitchCorrectionList.get(position);
    }

    public void addItem(String octave){
        pitchCorrectionList.add(octave);
        notifyDataSetChanged();
    }

    private boolean fetchUserUrlFromFirebase(String songIdx) {
        Log.v("user url fetch", songIdx);
        final boolean[] isUserUrlExists = new boolean[1];
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("User").child(userEmail).child("pitchCorrection").child(songIdx + ".mp3");

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
                        isUserUrlExists[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("음악 백그라운드 재생 실패", e.getMessage());
                        isUserUrlExists[0] = false;
                    }
                });
        return isUserUrlExists[0];
    }

    private void fetchAudioUrlFromFirebase(String songIdx) {
        Log.v("song url fetch", songIdx);
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("PitchCorrection").child(songIdx + ".mp3");

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



