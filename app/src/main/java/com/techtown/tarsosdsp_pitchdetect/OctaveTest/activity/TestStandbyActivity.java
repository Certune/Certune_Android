package com.techtown.tarsosdsp_pitchdetect.OctaveTest.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.SongListActivity;

public class TestStandbyActivity extends AppCompatActivity {

    private String userEmail;
    private String userSex;
    private String octaveHighLow;

    private String userVocalRange;
    private String maxUserPitch;
    private String minUserPitch;

    private Button highTestBtn;
    private Button lowTestBtn;
    private Button highTestCompleteBtn;
    private Button lowTestCompleteBtn;
    private Button finishTestBtn;
    private TextView textView_userOctaveDescription;
    private TextView textview_userOctave;

    private Boolean isHighDone;
    private Boolean isLowDone;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_octave_test_standby);
        Intent subIntent = getIntent();
        userEmail = subIntent.getStringExtra("userEmail");

        // 설정
        String SharedPrefFile = "com.example.android." + userEmail + ".SharedPreferences";
        mPreferences = getSharedPreferences(SharedPrefFile, MODE_PRIVATE);

        isHighDone = mPreferences.getBoolean("isHighDone", false);
        isLowDone = mPreferences.getBoolean("isLowDone", false);

        // 요소 할당
        highTestBtn = (Button) findViewById(R.id.button_highTest);
        lowTestBtn = (Button) findViewById(R.id.button_lowTest);
        highTestCompleteBtn = (Button) findViewById(R.id.button_highTestComplete);
        lowTestCompleteBtn = (Button) findViewById(R.id.button_highTestComplete);
        finishTestBtn = (Button) findViewById(R.id.button_finishTest);
        textview_userOctave = (TextView) findViewById(R.id.textview_userOctave);
        textView_userOctaveDescription = (TextView) findViewById(R.id.textview_userOctaveDesc);
        textview_userOctave.setVisibility(View.INVISIBLE);
        textView_userOctaveDescription.setVisibility(View.INVISIBLE);

        getUserInfo();
        checkIsTestComplete();

        highTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                octaveHighLow = "high";
                Intent intent = new Intent(getApplicationContext(), TestSingingActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("userSex", userSex);
                intent.putExtra("octaveHighLow", octaveHighLow);
                startActivity(intent);
            }
        });

        lowTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                octaveHighLow = "low";
                Intent intent = new Intent(getApplicationContext(), TestSingingActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("userSex", userSex);
                intent.putExtra("octaveHighLow", octaveHighLow);
                startActivity(intent);
            }
        });

        finishTestBtn.setEnabled(isLowDone && isHighDone);
        finishTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SongListActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("userSex", userSex);
                startActivity(intent);
            }
        });
    }

    private void checkIsTestComplete() {
        Drawable drawable = getResources().getDrawable(R.drawable.test_standby_test_complete_background);
        if (isHighDone)
            highTestCompleteBtn.setBackground(drawable);
        if (isLowDone)
            lowTestCompleteBtn.setBackground(drawable);

        if (isLowDone && isHighDone) {
            textview_userOctave.setVisibility(View.VISIBLE);
            textView_userOctaveDescription.setVisibility(View.VISIBLE);
            getUserVocalRange();
        }

    }

    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference docRef = database.collection("User").document(userEmail);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userSex = (String) document.getData().get("sex");
                        } else {
                            Log.d(TAG, "사용자 정보가 존재하지 않습니다.");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    private void getUserVocalRange() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference docRef = database.collection("User").document(userEmail);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            maxUserPitch = String.valueOf(document.getData().get("maxUserPitch"));
                            minUserPitch = String.valueOf(document.getData().get("minUserPitch"));
                            userVocalRange = minUserPitch + " ~ " + maxUserPitch;
                            textview_userOctave.setText(userVocalRange);
                        } else {
                            Log.d(TAG, "사용자 정보가 존재하지 않습니다.");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }
}
