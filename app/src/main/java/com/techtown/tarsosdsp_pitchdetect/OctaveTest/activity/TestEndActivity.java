package com.techtown.tarsosdsp_pitchdetect.OctaveTest.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.techtown.tarsosdsp_pitchdetect.R;

public class TestEndActivity extends AppCompatActivity {

    private String userEmail;
    private String octaveHighLow;

    private TextView endText;
    private Button resetBtn;

    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_octave_test_end);

        Intent intent =  getIntent();
        userEmail = intent.getStringExtra("userEmail");
        octaveHighLow = intent.getStringExtra("octaveHighLow");

        // 설정
        String SharedPrefFile = "com.example.android."+userEmail+".SharedPreferences";
        mPreferences = getSharedPreferences(SharedPrefFile, MODE_PRIVATE);

        endText = (TextView) findViewById(R.id.endText);
        resetBtn = (Button) findViewById(R.id.resetBtn);

        ApplySharedPreference();
        SetEndText();

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TestStandbyActivity.class);
                intent.putExtra("userEmail", userEmail);
                startActivity(intent);
            }
        });
    }

    private void SetEndText() {
        if (octaveHighLow.equals("high"))
            endText.setText("고음 테스트가 종료되었습니다.");
        else if (octaveHighLow.equals("low"))
            endText.setText("저음 테스트가 종료되었습니다.");
    }

    private void ApplySharedPreference() {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        if (octaveHighLow.equals("high"))
            preferencesEditor.putBoolean("isHighDone", true);
        else if (octaveHighLow.equals("low"))
            preferencesEditor.putBoolean("isLowDone", true);
        preferencesEditor.apply();
    }
}