package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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

public class OctaveTestActivity extends AppCompatActivity {

    private String userEmail;
    private String userSex;
    private String octaveHighLow;

    private String userVocalRange;
    private String maxUserPitch;
    private String minUserPitch;

    private Button highTestBtn;
    private Button lowTestBtn;
    private Button finishTestBtn;
    private TextView textview_userOctave;

    private Boolean isHighDone = false;
    private Boolean isLowDone = false;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isHighDone = savedInstanceState.getBoolean("isHighDone");
            isLowDone = savedInstanceState.getBoolean("isLowDone");
            Log.v("saved", "인스턴스 호출 완료");
        }

        setContentView(R.layout.activity_octave_test);
        Intent subIntent = getIntent();
        userEmail = subIntent.getStringExtra("userEmail");

        highTestBtn = (Button) findViewById(R.id.button_highTest);
        lowTestBtn = (Button) findViewById(R.id.button_lowTest);
        finishTestBtn = (Button) findViewById(R.id.button_finishTest);
        textview_userOctave = (TextView) findViewById(R.id.textView_octaveRange);

        Log.v("oncreate", "Oncreate 시작");
        Log.v("HIGH", String.valueOf(isHighDone));
        Log.v("LOW", String.valueOf(isLowDone));
        getUserInfo();

        highTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                octaveHighLow = "high";
                isHighDone = true;
                Intent intent = new Intent(getApplicationContext(), OctaveTestSingingActivity.class);
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
                isLowDone = true;
                Intent intent = new Intent(getApplicationContext(), OctaveTestSingingActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("userSex", userSex);
                intent.putExtra("octaveHighLow", octaveHighLow);
                startActivity(intent);
            }
        });

        finishTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : lowTest, highTest 끝났는지 확인 필요
                Intent intent = new Intent(getApplicationContext(), SongListActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("userSex", userSex);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v("onStart", "onStart 시작");
        Log.v("HIGH", String.valueOf(isHighDone));
        Log.v("LOW", String.valueOf(isLowDone));

        if (isHighDone && isLowDone) {
            getUserVocalRange();
            Log.v("실행 완", userVocalRange);
            textview_userOctave.setText(userVocalRange);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("onResume", "onResume 시작");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isHighDone = savedInstanceState.getBoolean("isHighDone");
        isLowDone = savedInstanceState.getBoolean("isLowDone");
        Log.v("restore", "restore 시작");
        Log.v("HIGH", String.valueOf(isHighDone));
        Log.v("LOW", String.valueOf(isLowDone));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.v("onSave", "onsaved 시작");
        Log.v("HIGH", String.valueOf(isHighDone));
        Log.v("LOW", String.valueOf(isLowDone));
        outState.putBoolean("isHighDone", isHighDone);
        outState.putBoolean("isLowDone", isLowDone);
        super.onSaveInstanceState(outState);
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
                            Log.v("유저 성별", userSex);
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
                            userVocalRange = "최저 음역대 : " + minUserPitch + "\n" + "최고 음역대 : " + maxUserPitch;
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
