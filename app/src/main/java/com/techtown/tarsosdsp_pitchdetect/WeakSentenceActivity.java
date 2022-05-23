package com.techtown.tarsosdsp_pitchdetect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class WeakSentenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weak_sentence);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // 프래그먼트매니저를 통해 사용
        SongRecordActivity songRecordActivity = new SongRecordActivity(); // 객체 생성
        //transaction.replace(R.id.frameLayout, ); //layout, 교체될 layout
        transaction.commit(); //commit으로 저장 하지 않으면 화면 전환이 되지 않음
    }
}