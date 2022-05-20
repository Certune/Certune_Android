package com.techtown.tarsosdsp_pitchdetect.score.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;

public class SingingResult extends AppCompatActivity {

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singing_result);
    }
}