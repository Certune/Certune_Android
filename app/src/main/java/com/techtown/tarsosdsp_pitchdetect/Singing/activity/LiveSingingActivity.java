package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.gridlayout.widget.GridLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.MusicDto;
import static com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx.noteToIdx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LiveSingingActivity extends AppCompatActivity {

    private String userEmail;
    private Boolean isShifting;

    private String songName = "신호등";
    private String songLowKey = "A#4";
    private String songHighKey = "D#5";
    private Double songEndTime = 123.23;

    ArrayList<NoteDto> noteDtoList = new ArrayList<>();

    private HorizontalScrollView scrollView;
    GridLayout gridLayout;
    Button cell;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_singing);
      
        /*
        Intent subIntent = new Intent();
        songName = subIntent.getStringExtra("songName");
        userEmail = subIntent.getStringExtra("userEmail");
        songLowKey = subIntent.getStringExtra("songLowKey");
        songHighKey = subIntent.getStringExtra("songHighKey");
        isShifting = subIntent.getBooleanExtra("isShifting", false);
        */

        scrollView = findViewById(R.id.horizontalScrollView);
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setUseDefaultMargins(false);

        getSongEndTime();

    }

    public void setCell(int noteIdx, int startTime, int endTime) {
        Log.v("노트 인덱스", String.valueOf(noteIdx));
        Log.v("시작 시간", String.valueOf(startTime));
        Log.v("종료 시간", String.valueOf(endTime));
        GridLayout.Spec cellRow = GridLayout.spec(noteIdx);
        GridLayout.Spec cellCol = GridLayout.spec(startTime, endTime - startTime);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams(cellRow, cellCol);
        param.height = gridLayout.getHeight() / gridLayout.getRowCount();
        param.width = endTime - startTime;
        param.setGravity(Gravity.FILL_HORIZONTAL);

        cell = new Button(this);
        cell.setEnabled(false);
        cell.setLayoutParams(param);
        cell.setBackgroundColor(Color.GRAY); // 이거 없애면 버튼 홀쭉해짐

        gridLayout.addView(cell, param);
    }

    public void settingView() {
        int songLowKeyIdx = noteToIdx(songLowKey);
        int songHighKeyIdx = noteToIdx(songHighKey);
        gridLayout.setRowCount(songHighKeyIdx - songLowKeyIdx + 1);
        Log.v("row 개수", String.valueOf(gridLayout.getRowCount()));
        gridLayout.setColumnCount((int) Math.round(songEndTime * 100));

        for (NoteDto noteDto : noteDtoList) {
            int noteIdx = noteToIdx(noteDto.getNote()) - songLowKeyIdx;
            int startTime = (int) Math.round(Double.parseDouble(noteDto.getStartTime()) * 100);
            int endTime = (int) Math.round(Double.parseDouble(noteDto.getEndTime()) * 100);
            setCell(noteIdx, startTime, endTime);
        }
    }

    private void setScrollSettings() {
        LinearInterpolator interpolator = new LinearInterpolator();
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollX", 1000000);
                objectAnimator.setDuration(Math.round(songEndTime * 100000)); // TODO : SONG 길이에 맞춰서 넣어줘야 함
                objectAnimator.setInterpolator(new LinearInterpolator());
                objectAnimator.start();
            }
        });
    }

    private void getSongEndTime() {

        database.collection("Song").document("신호등")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        songEndTime = (Double) document.getData().get("endTime");
                        setScrollSettings();
                        List list = (List) Objects.requireNonNull(document.getData()).get("sentence");

                        for (int i = 0; i < list.size(); i++) {
                            HashMap<String, ArrayList<HashMap<String, Object>>> map = (HashMap) list.get(i);
                            ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");

                            for (HashMap<String, Object> notemap : arrayMap) {
                                NoteDto noteDto = new NoteDto(
                                        String.valueOf(notemap.get("start_time")),
                                        String.valueOf(notemap.get("end_time")),
                                        String.valueOf(notemap.get("note"))
                                );
                                noteDtoList.add(noteDto);
                            }
                        }
                        settingView();

                    } catch (Exception e) {
                        Log.e("endTimeImport", e.getMessage());
                    }
                }
            }
        });
    }

}