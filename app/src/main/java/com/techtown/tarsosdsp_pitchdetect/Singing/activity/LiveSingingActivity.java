package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

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

    public void setThis() {
        // https://stackoverflow.com/questions/35924875/how-to-use-gridlayout-spec-for-a-column-extending-multiple-rows
        // face view takes 2 rows, 1 column -- zero index based
        GridLayout.Spec faceRow = GridLayout.spec(0, 2); // starts row 0, takes 2 rows
        GridLayout.Spec faceCol = GridLayout.spec(0); // starts col 0, takes 1 col

        GridLayout.Spec titleRow = GridLayout.spec(0); // starts row 0, takes 1 row
        GridLayout.Spec titleCol = GridLayout.spec(1, 3); // starts col 1, takes 3 cols

        GridLayout.Spec plusRow = GridLayout.spec(1); // starts row 1, takes 1 row
        GridLayout.Spec plusCol = GridLayout.spec(1); // starts col 1, takes 1 col

        GridLayout.Spec minusRow = GridLayout.spec(1); // starts row 1, takes 1 row
        GridLayout.Spec minusCol = GridLayout.spec(2); // starts col 1, takes 1 col

        GridLayout.Spec checkRow = GridLayout.spec(1); // starts row 1, takes 1 row
        GridLayout.Spec checkCol = GridLayout.spec(3); // starts col 1, takes 1 col

// create the LayoutParams using our row/col for each view
        GridLayout.LayoutParams faceParams = new GridLayout.LayoutParams(faceRow, faceCol);
        faceParams.setGravity(Gravity.FILL_VERTICAL); // fill vertical so we take up the full 2 rows
// dummy text views to fill some space
        TextView faceText = new TextView(this);
        faceText.setPadding(32, 32, 32, 32); // add some random padding to make the views bigger
        faceText.setLayoutParams(faceParams);
        faceText.setText("FACE");
        faceText.setGravity(Gravity.CENTER);
        faceText.setBackgroundColor(Color.RED);
        gridLayout.addView(faceText, faceParams);

        GridLayout.LayoutParams titleParams = new GridLayout.LayoutParams(titleRow, titleCol);
        titleParams.setGravity(Gravity.FILL_HORIZONTAL); // fill horizontal so we take up the full 3 columns
        TextView titleText = new TextView(this);
        titleText.setPadding(32, 32, 32, 32);
        titleText.setLayoutParams(titleParams);
        titleText.setText("TITLE");
        titleText.setGravity(Gravity.CENTER);
        titleText.setBackgroundColor(Color.BLUE);
        gridLayout.addView(titleText, titleParams);

        GridLayout.LayoutParams minusParams = new GridLayout.LayoutParams(minusRow, minusCol);
        TextView minusText = new TextView(this);
        minusText.setPadding(32, 32, 32, 32);
        minusText.setLayoutParams(minusParams);
        minusText.setText("MIN");
        minusText.setGravity(Gravity.CENTER);
        minusText.setBackgroundColor(Color.YELLOW);
        gridLayout.addView(minusText, minusParams);

        GridLayout.LayoutParams plusParams = new GridLayout.LayoutParams(plusRow, plusCol);
        TextView plusText = new TextView(this);
        plusText.setPadding(32, 32, 32, 32);
        plusText.setLayoutParams(plusParams);
        plusText.setText("PLS");
        plusText.setGravity(Gravity.CENTER);
        plusText.setBackgroundColor(Color.GREEN);
        gridLayout.addView(plusText, plusParams);

        GridLayout.LayoutParams checkParams = new GridLayout.LayoutParams(checkRow, checkCol);
        TextView checkText = new TextView(this);
        checkText.setPadding(32, 32, 32, 32);
        checkText.setLayoutParams(faceParams);
        checkText.setText("CHK");
        checkText.setGravity(Gravity.CENTER);
        checkText.setBackgroundColor(Color.MAGENTA);
        gridLayout.addView(checkText, checkParams);
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

                        // TODO : i 최종 값 수정해야 함(현재는 든 게 없어서 empty String 에러
                        for (int i = 0; i < 1; i++) {
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