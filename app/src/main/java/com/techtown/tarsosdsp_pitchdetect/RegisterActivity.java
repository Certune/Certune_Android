package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    private ImageButton backBtn;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputName;

    String userSex;
    private RadioGroup sexRadioGroup;
    private RadioButton femaleRadioBtn, maleRadioBtn;


    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        backBtn = (ImageButton) findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        inputEmail = (EditText) findViewById(R.id.edittext_email);
        inputPassword = (EditText) findViewById(R.id.edittext_password);
        inputName = (EditText) findViewById(R.id.edittext_name);

        userSex = "";
        femaleRadioBtn = (RadioButton) findViewById(R.id.radioBtn_female);
        maleRadioBtn = (RadioButton) findViewById(R.id.radioBtn_male);
        sexRadioGroup = (RadioGroup) findViewById(R.id.radioBtnGroup);
        sexRadioGroup.setOnCheckedChangeListener(sexRadioGroupButtonChangeListener);

        registerBtn = (Button) findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputEmail.getText().toString().equals("") && !inputPassword.getText().toString().equals("") && !inputName.getText().toString().equals("") && !userSex.equals("")) {
                    // 이메일과 비밀번호, 이름, 성별 입력칸이 공백이 아닌 경우
                    createUser(inputEmail.getText().toString(), inputPassword.getText().toString(), inputName.getText().toString(), userSex);
                } else {
                    // 이메일과 비밀번호가 공백인 경우
                    Toast.makeText(RegisterActivity.this, "계정과 비밀번호를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    RadioGroup.OnCheckedChangeListener sexRadioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.radioBtn_female) {
                userSex = "female";
            } else if(i == R.id.radioBtn_male) {
                userSex = "male";
            }
        }
    };

    private void createUser(String email, String password, String name, String sex) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String, String> user = new HashMap<>();
                        user.put("name", name);
                        user.put("sex", sex);

                        if (task.isSuccessful()) {
                            // 회원가입 성공시
                            db.collection("User").document(email)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                            Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 계정이 중복된 경우
                            Toast.makeText(RegisterActivity.this, "이미 존재하는 계정입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}