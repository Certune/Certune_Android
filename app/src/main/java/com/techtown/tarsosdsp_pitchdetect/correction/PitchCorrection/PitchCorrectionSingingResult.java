package com.techtown.tarsosdsp_pitchdetect.correction.PitchCorrection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.techtown.tarsosdsp_pitchdetect.R;

public class PitchCorrectionSingingResult extends AppCompatActivity {

    Button scoreTextButton;
    Button endButton;

    String noteScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_correction_result);

        Intent subIntent = getIntent();
        noteScore = subIntent.getStringExtra("noteScore");

        scoreTextButton = findViewById(R.id.scoreTextButton);
        endButton = findViewById(R.id.endButton);

        String noteScoreText = Math.round(Double.parseDouble(noteScore)) + "점";
        scoreTextButton.setText(noteScoreText);

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PitchCorrection.class);
                startActivity(intent);
            }
        });
    }
}