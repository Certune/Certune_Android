package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import static com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx.noteToIdx;

import static android.content.ContentValues.TAG;

import android.graphics.drawable.Drawable;
import static com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx.noteToIdx;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SingingStandbyActivity extends AppCompatActivity {

    private String userEmail;
    private String songName;
    private String singerName;

    private String lyrics;

    private String songLowKey;
    private String songHighKey;
    private String userLowKey;
    private String userHighKey;

    /* shifting 관련 정보 */
    private Boolean isShifting = false;
    private int lowKeyIdx;
    private int midKeyIdx;
    private int highKeyIdx;
    ArrayList<Integer> midNoteIdxList = new ArrayList<>();
    ArrayList<Integer> shiftingList = new ArrayList<>();
    private int shiftingIdx = 0;

    TextView songNameTextView;
    TextView singerNameTextView;
    TextView lyricsTextView;
    TextView songLowKeyTextView;
    TextView songHighKeyTextView;
    TextView userLowKeyTextView;
    TextView userHighKeyTextView;

    Switch shiftingSwitch;
    Button singingBtn;
    ImageButton backBtn;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singing_standby);

        Intent subIntent = getIntent();
        userEmail = subIntent.getStringExtra("userEmail");
        songName = subIntent.getStringExtra("songName");

        songNameTextView = findViewById(R.id.songName);
        singerNameTextView = findViewById(R.id.singerName);
        singerNameTextView.bringToFront();
        lyricsTextView = findViewById(R.id.lyrics);
        songLowKeyTextView = findViewById(R.id.songKeyLow);
        songHighKeyTextView = findViewById(R.id.songKeyHigh);
        userLowKeyTextView = findViewById(R.id.userKeyLow);
        userHighKeyTextView = findViewById(R.id.userKeyHigh);

        shiftingSwitch = findViewById(R.id.shiftingSwitch);
        singingBtn = findViewById(R.id.singingButton);
        backBtn = findViewById(R.id.backButton);

        songNameTextView.setText(songName);
        findSongInfo();
        findUserKeyInfo();

        shiftingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isShifting = isChecked;
            }
        });

        singingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LiveSingingActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("songName", songName);
                intent.putExtra("singerName", singerName);
                intent.putExtra("isShifting", isShifting);
                intent.putExtra("shiftingIdx", shiftingIdx);
                intent.putExtra("songLowKey", songLowKey);
                intent.putExtra("songHighKey", songHighKey);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SongListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void findSongInfo() {
        DocumentReference docRef = database.collection("Song").document(songName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        // 가수 정보 가져오기
                        singerName = (String) document.getData().get("singer");

                        // 가사 정보 가져오기
                        List list = (List) Objects.requireNonNull(document.getData()).get("sentence");
                        HashMap<String, String> firstMap = (HashMap) list.get(0);
                        String firstLyrics = firstMap.get("lyrics");
                        HashMap<String, String> secondMap = (HashMap) list.get(1);
                        String secondLyrics = secondMap.get("lyrics");
                        lyrics = firstLyrics + "\n\n" + secondLyrics;

                        // 노래 최저, 최고음 가져오기
                        songLowKey = (String) document.getData().get("lowKey");
                        songHighKey = (String) document.getData().get("highKey");

                        singerNameTextView.setText(singerName);
                        lyricsTextView.setText(lyrics);
                        songLowKeyTextView.setText(songLowKey);
                        songHighKeyTextView.setText(songHighKey);
                    } catch (Exception e) {
                        Log.e("Song 정보 import", "노래 정보 로딩에 실패했습니다");
                    }
                }
            }
        });
    }

    private void changeUserKeyColor() {
        int songHighKeyIdx = noteToIdx(songHighKey);
        int songLowKeyIdx = noteToIdx(songLowKey);
        int userHighKeyIdx = noteToIdx(userHighKey);
        int userLowKeyIdx = noteToIdx(userLowKey);

        Drawable drawable = getResources().getDrawable(R.drawable.singing_standby_keybutton_background_danger);
        if (userHighKeyIdx < songHighKeyIdx || userLowKeyIdx > songLowKeyIdx)
            userHighKeyTextView.setBackground(drawable);

    }

    private void findUserKeyInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
            DocumentReference docRef = database.collection("User").document(userEmail);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userLowKey = (String) document.getData().get("minUserPitch");
                            userHighKey = (String) document.getData().get("maxUserPitch");

                            lowKeyIdx = noteToIdx(userLowKey);
                            highKeyIdx = noteToIdx(userLowKey);
                            midKeyIdx = (lowKeyIdx + highKeyIdx) / 2;
                            compareMidNote();

                            userLowKeyTextView.setText(userLowKey);
                            userHighKeyTextView.setText(userHighKey);

                            changeUserKeyColor();
                        } else {
                            Log.e("유저 정보 오류", "사용자 정보가 존재하지 않습니다.");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }

            });
        }
    }

    private void compareMidNote() {
        database.collection("Singing").document(songName).collection("shifting")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        shiftingList.add(Integer.parseInt(document.getId()));
                        String midNoteIdx = document.getData().get("midKey").toString();
                        midNoteIdxList.add(noteToIdx(midNoteIdx));
                    }

                    shiftingIdx = 0;
                    int minVal = abs(midNoteIdxList.get(0) - midKeyIdx);
                    for (int i=0; i<shiftingList.size(); i++){
                        int curVal = abs(midNoteIdxList.get(i) - midKeyIdx);
                        if (curVal < minVal) {
                            minVal = curVal;
                            shiftingIdx = shiftingList.get(i);
                        }
                    }
                    Log.v("맞춤 note shift", String.valueOf(shiftingIdx));
                }
                else {
                    Log.d("TAG", "Error getting documents");
                }
            }
        });
    }
}