package com.techtown.tarsosdsp_pitchdetect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OctaveTestEndActivity extends AppCompatActivity {

    private String userEmail;
    private String userSex;
    private String octaveHighLow;

    private TextView endText;
    private Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_octave_test_end);
        Intent intent =  getIntent();
        userEmail = intent.getStringExtra("userEmail");
        userSex = intent.getStringExtra("userSex");
        octaveHighLow = intent.getStringExtra("octaveHighLow");
        setResult(RESULT_OK, intent);

        endText = (TextView) findViewById(R.id.endText);
        resetBtn = (Button) findViewById(R.id.resetBtn);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OctaveTestEndActivity.this, OctaveTestActivity.class);
                startActivity(intent);
            }
        });

        if (octaveHighLow.equals("high"))
            endText.setText("고음 테스트를 완료하셨습니다");
        else if (octaveHighLow.equals("low"))
            endText.setText("저음 테스트를 완료하셨습니다.");
    }
}