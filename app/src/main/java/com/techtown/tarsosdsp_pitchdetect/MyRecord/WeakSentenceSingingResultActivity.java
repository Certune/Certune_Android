package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.techtown.tarsosdsp_pitchdetect.R;

public class WeakSentenceSingingResultActivity extends AppCompatActivity {

    Button scoreTextButton;
    Button endButton;

    String songName;
    String sentenceIdx;
    String noteScore;
    String rhythmScore;
    String totalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weak_sentence_singing_result);

        Intent subIntent = getIntent();
        noteScore = subIntent.getStringExtra("noteScore");
        rhythmScore = subIntent.getStringExtra("rhythmScore");
        totalScore = subIntent.getStringExtra("totalScore");

        scoreTextButton = findViewById(R.id.scoreTextButton);
        endButton = findViewById(R.id.endButton);

        scoreTextButton.setText(totalScore);
        
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WeakSentenceListActivity.class);
                intent.putExtra("songName", "신호등");
                intent.putExtra("singerName", "이무진");
                startActivity(intent);
            }
        });
    }
}