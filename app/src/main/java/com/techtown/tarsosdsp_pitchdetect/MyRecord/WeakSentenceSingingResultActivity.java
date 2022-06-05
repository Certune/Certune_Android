package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

        setProgressColors();

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

    private void setProgressColors() {
        // note progress
        int noteScoreProgress = (int) Math.round(Double.parseDouble(noteScore));

        Drawable noteProgressDrawable;
        if (noteScoreProgress < 40)
            noteProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_bad, null);
        else if (noteScoreProgress < 70)
            noteProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_soso, null);
        else
            noteProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null);

        noteScoreProgressbar.setProgress(noteScoreProgress);
        noteScoreProgressbar.setProgressDrawable(noteProgressDrawable);

        // rhythm progress
        int rhythmScoreProgress = (int) Math.round(Double.parseDouble(rhythmScore));

        Drawable rhythmProgressDrawable;
        if (rhythmScoreProgress < 40)
            rhythmProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_bad, null);
        else if (rhythmScoreProgress < 70)
            rhythmProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar_soso, null);
        else
            rhythmProgressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null);

        rhythmScoreProgressbar.setProgress(rhythmScoreProgress);
        rhythmScoreProgressbar.setProgressDrawable(rhythmProgressDrawable);
    }
}