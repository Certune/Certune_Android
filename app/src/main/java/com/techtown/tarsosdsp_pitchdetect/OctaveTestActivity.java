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

    private Button highTestBtn;
    private Button lowTestBtn;
    private TextView textview_userOctave;

    private Boolean isHighDone = false;
    private Boolean isLowDone = false;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_octave_test);
        Intent subIntent = getIntent();
        userEmail = subIntent.getStringExtra("userEmail");

        highTestBtn = (Button) findViewById(R.id.button_highTest);
        lowTestBtn = (Button) findViewById(R.id.button_lowTest);
        textview_userOctave = (TextView) findViewById(R.id.textview_userOctave);

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
        getUserInfo();

        if (isHighDone && isLowDone) {
            // TODO : 사용자 음역대 TEXTVIEW에 반영
            textview_userOctave.setText("사용자 음역대");
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

}
