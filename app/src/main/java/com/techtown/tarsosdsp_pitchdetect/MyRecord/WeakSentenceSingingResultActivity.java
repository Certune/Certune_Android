package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.techtown.tarsosdsp_pitchdetect.R;

public class WeakSentenceSingingResultActivity extends AppCompatActivity {

    Button totalscoreButton;
    Button endButton;
    TextView noteScoreTextView;
    TextView rhythmScoreTextView;
    ProgressBar noteScoreProgressbar;
    ProgressBar rhythmScoreProgressbar;

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

        totalscoreButton = findViewById(R.id.scoreTextButton);
        noteScoreTextView = findViewById(R.id.noteScoreTextView);
        rhythmScoreTextView = findViewById(R.id.rhythmScoreTextView);
        noteScoreProgressbar = findViewById(R.id.note_progressbar);
        rhythmScoreProgressbar = findViewById(R.id.rhythm_progressbar);
        endButton = findViewById(R.id.endButton);

        String totalScoreText = Math.round(Double.parseDouble(totalScore)) +"점";
        String noteScoreText = Math.round(Double.parseDouble(noteScore)) +"점";
        String rhythmScoreText = Math.round(Double.parseDouble(rhythmScore)) +"점";

        totalscoreButton.setText(totalScoreText);
        noteScoreTextView.setText(noteScoreText);
        rhythmScoreTextView.setText(rhythmScoreText);
        noteScoreProgressbar.setProgress((int) Math.round(Double.parseDouble(noteScore)));
        rhythmScoreProgressbar.setProgress((int) Math.round(Double.parseDouble(rhythmScore)));

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