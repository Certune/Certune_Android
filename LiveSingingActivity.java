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
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LiveSingingActivity extends AppCompatActivity {
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();
    String songName = "신호등";
    int[] noteHeight = {0, 1, 2, 0};

    GridLayout gridLayout;

    Button cell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_singing);

        gridLayout = findViewById(R.id.gridLayout);
        settingView();

    }

    public void setCell(int timeLine, int pitchLine) {
        cell = new Button(this);
        cell.setEnabled(false);
        cell.setWidth(100000);
        cell.setHeight(10);

        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;

        param.rowSpec = GridLayout.spec(pitchLine);
        param.columnSpec = GridLayout.spec(timeLine);

        cell.setLayoutParams(param);
        gridLayout.addView(cell);
    }

    public void settingView() {
        gridLayout.setRowCount(18);
        gridLayout.setColumnCount(10);

        for(int i=0; i<noteHeight.length; i++) {
            setCell(i, noteHeight[i]);
        }


    }


}