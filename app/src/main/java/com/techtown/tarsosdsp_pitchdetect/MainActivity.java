package com.techtown.tarsosdsp_pitchdetect;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.techtown.tarsosdsp_pitchdetect.fragment.HomeFragment;
import com.techtown.tarsosdsp_pitchdetect.fragment.MyRecordFragment;
import com.techtown.tarsosdsp_pitchdetect.fragment.ResultFragment;
import com.techtown.tarsosdsp_pitchdetect.fragment.SingFragment;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;



public class MainActivity extends AppCompatActivity  {


    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줌
    private FragmentManager fragmentManager =getSupportFragmentManager();

    private HomeFragment homeFragment = new HomeFragment();
    private SingFragment singFragment = new SingFragment();
    private MyRecordFragment myRecordFragment = new MyRecordFragment();
    private ResultFragment resultFragment = new ResultFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

       BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);

        //맨 처음 시작할 화면 지정
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container,homeFragment).commitAllowingStateLoss();

        // bottomNavigationView의 아이템이 선택될 때 호출될 리스너 등록
        bottomNavigationView.setOnItemSelectedListener(new
                NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home: {
                   transaction.replace(R.id.container, homeFragment).commitAllowingStateLoss();
                   break;
                }
                case R.id.navigation_sing: {
                    transaction.replace(R.id.container, singFragment).commitAllowingStateLoss();
                    break;
                }
                case R.id.navigation_myRecord: {
                    transaction.replace(R.id.container, myRecordFragment).commitAllowingStateLoss();

                    break;
                }
                case R.id.navigation_result: {
                    transaction.replace(R.id.container, resultFragment).commitAllowingStateLoss();
                    break;
                }
            }
            return true;
            }
        });
    }
}
