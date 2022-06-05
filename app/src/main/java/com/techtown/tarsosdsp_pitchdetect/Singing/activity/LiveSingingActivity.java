package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import static com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx.noteToIdx;
import static com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessPitch.processPitch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SentenceInfoDto;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingNoteDto;
import com.techtown.tarsosdsp_pitchdetect.global.MusicDto;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessPitch;

import java.io.File;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;

public class LiveSingingActivity extends AppCompatActivity {
    // 로그인된 유저의 이름, 이메일, uid 정보
    String userName;
    String userEmail = "nitronium007@gmail.com";
    String userSex;
    String uid;

    String songName = "신호등";
    String singerName;
    Boolean isShifting;
    private Double songEndTime;

    String musicUrl;
    private long startTime;

    AudioDispatcher dispatcher;
    TarsosDSPAudioFormat tarsosDSPAudioFormat;

    LineChart mChart;

    Map<Double, String> map; // {key : octav}
    Map<Double, String> musicMap;

    ArrayList<Double> startTimeList;
    ArrayList<Double> endTimeList;
    ArrayList<MusicDto> musicTotalInfoList;

    MediaPlayer mediaPlayer;

    File file;

    boolean isRecording = false;
    String filename = "singingResult.wav";

    Handler timeHandler = new Handler();

    ArrayList<SingingNoteDto> noteDtoList = new ArrayList<>();
    ArrayList<SentenceInfoDto> sentenceInfoList = new ArrayList<>();

    private HorizontalScrollView scrollView;
    GridLayout gridLayout;
    Button cell;
    LoadingDialog dialog;

    TextView currentLyric;
    TextView nextLyric;

    int firstNoteStartTime;
    Long showStartTime;

    Boolean isNoteLoad = false;
    Boolean isMusicLoad = false;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    final Handler handlerSong = new Handler();
    final Handler handlerSetting = new Handler();
    final Handler handlerSetting2 = new Handler();


    final Runnable runnableSong = new Runnable() {
        @Override
        public void run() {
            setScrollSettings();
            for (SingingNoteDto noteDto : noteDtoList) {
                int noteIdx = noteToIdx(noteDto.getNote());
                int startTime = (int) Math.round(Double.parseDouble(noteDto.getStartTime()) * 10);
                int endTime = (int) Math.round(Double.parseDouble(noteDto.getEndTime()) * 10);
                boolean isNote = noteDto.getIsNote();
                Log.v("isNote", String.valueOf(isNote));
                setCell(noteIdx, startTime, endTime, isNote, false);
            }
            isNoteLoad = true;
            checkLoading();
        }
    };

    final Runnable runnableSetting = new Runnable() {
        @Override
        public void run() {
            for (int j = 0; j < gridLayout.getRowCount(); j++) {
                setCell(j, 0, 1, false, true);
            }
        }
    };

    final Runnable runnableSetting2 = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < gridLayout.getColumnCount(); i++) {
                setCell(0, i, i + 1, false, true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_singing);

        handlerSetting.post(runnableSetting);
        handlerSetting2.post(runnableSetting2);

        // Get xml instances
//        mChart = findViewById(R.id.chart);
        scrollView = findViewById(R.id.horizontalScrollView);
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setUseDefaultMargins(false);

        currentLyric = findViewById(R.id.currentLyricTextView);
        nextLyric = findViewById(R.id.nextLyricTextView);

        dialog = new LoadingDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        fetchAudioUrlFromFirebase();
        File sdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        file = new File(sdCard, filename);

        // Basic Setting for tarsosDSP AudioFormat
        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

//
//        Intent subIntent = new Intent();
//        songName = subIntent.getStringExtra("songName");
//        userEmail = subIntent.getStringExtra("userEmail");
//        isShifting = subIntent.getBooleanExtra("isShifting", false);

        getSongInfo();
        getSongEndTime();
//        microphoneOn();
//        setChart();
//        setAxis();

        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentLyric.setText("가려한 날 막아서네 난 갈 길이 먼데");
                nextLyric.setText("새빨간 얼굴로 화를 냈던 친구가 생각나네");
            }
        }, 30000);

        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentLyric.setText("새빨간 얼굴로 화를 냈던 친구가 생각나네");
                nextLyric.setText("이미 난 발걸음을 떼었지만");
            }
        }, 36000);
    }

    public void createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(musicUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    isMusicLoad = true;
                    checkLoading();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("MEDIAPLAYER", e.getMessage());
        }
    }

    // stream music directly from firebase
    private void fetchAudioUrlFromFirebase() {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("songs").child(songName).child("song.mp3");

        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        musicUrl = uri.toString();
                        createMediaPlayer();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("음악 백그라운드 재생 실패", e.getMessage());
                    }
                });
    }

    private void getSongInfo() {
        // get music info from database
        database.collection("Song").document(songName).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        try {
                            List list = (List) Objects.requireNonNull(document.getData()).get("sentence");
                            startTimeList = new ArrayList<>();
                            endTimeList = new ArrayList<>();
                            musicTotalInfoList = new ArrayList<>();
                            for (int i = 0; i < Objects.requireNonNull(list).size(); i++) {
                                HashMap<String, ArrayList<HashMap<String, Object>>> map = (HashMap) list.get(i);
                                ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");

                                ArrayList<NoteDto> noteDtoArrayList = new ArrayList<>();
                                for (HashMap<String, Object> notemap : arrayMap) {
                                    NoteDto noteDto = new NoteDto(
                                            String.valueOf(notemap.get("start_time")),
                                            String.valueOf(notemap.get("end_time")),
                                            String.valueOf(notemap.get("note"))
                                    );
                                    noteDtoArrayList.add(noteDto);
                                }

                                MusicDto musicDto = new MusicDto(
                                        String.valueOf(map.get("start_time")),
                                        String.valueOf(map.get("end_time")),
                                        String.valueOf(map.get("lyrics")),
                                        noteDtoArrayList
                                );
                                // ArrayList에 소절별 시작 시간과 끝 시간 담기
                                startTimeList.add(Double.parseDouble(musicDto.getStartTime()));
                                endTimeList.add(Double.parseDouble(musicDto.getEndTime()));
                                // TODO : MusicDto 전체 받아오는 LIST 만들기(점수 산출용)
                                musicTotalInfoList.add(musicDto);

                                NoteDto noteDtoTest = musicDto.getNotes().get(0);
                            }
                        } catch (Exception e) {
                            Log.v("ERROR", "not enough records for calculating");
                        }
                    }
                }
        );
    }

    public void checkLoading() {
        // TODO : mp3 player 로딩 완료창도 추가
        Log.v("로딩완?", isNoteLoad + " : " + isMusicLoad);
        if (isNoteLoad && isMusicLoad) {
            dialog.dismiss();
            showStartTime = System.currentTimeMillis();
            mediaPlayer.start();

            final Handler handlerStart = new Handler();

            handlerStart.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setScrollSettings();
                }
            }, firstNoteStartTime * 1000L);
        }
    }

    public void setCell(int noteIdx, int startTime, int endTime, boolean isNote, boolean isPrepare) {
        Log.v("노트 인덱스", String.valueOf(noteIdx));
        Log.v("시작 시간", String.valueOf(startTime));
        Log.v("종료 시간", String.valueOf(endTime));
        GridLayout.Spec cellRow = GridLayout.spec(noteIdx, 1, 1f);
        GridLayout.Spec cellCol = GridLayout.spec(startTime);

        GridLayout.LayoutParams param = new GridLayout.LayoutParams(cellRow, cellCol);
        param.height = 0;
        param.width = (endTime - startTime) * 10;
        param.setGravity(Gravity.FILL_HORIZONTAL);

        cell = new Button(this);
        cell.setEnabled(false);
        cell.setLayoutParams(param);
        if (isNote) {
            cell.setBackgroundColor(Color.GRAY);
            Log.v("true일 때 색깔", "얍");
        } else {
            cell.setBackgroundColor(Color.parseColor("#212121")); // 이거 없애면 버튼 홀쭉해짐
            Log.v("FALSE일 때 색깔", "얍");
        }
        if (isPrepare)
            cell.setBackgroundColor(Color.parseColor("#212121"));

        gridLayout.addView(cell, param);
    }

    public void settingView() {
        Log.v("row 개수", String.valueOf(gridLayout.getRowCount()));
        gridLayout.setColumnCount((int) Math.round(songEndTime * 10));

        handlerSong.post(runnableSong);
    }

    private void setScrollSettings() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollX", 100000);
                int durationTime = Math.round(11 * 100000); // TODO : SONG 길이에 맞춰서 넣어줘야 함
                objectAnimator.setDuration(durationTime);
                objectAnimator.setInterpolator(new LinearInterpolator());
                objectAnimator.start();
            }
        });
    }

    private void getSongEndTime() {

        database.collection("Singing").document("신호등")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
//                    try {
//                        songEndTime = Double.parseDouble((String) document.getData().get("endTime"));
                    songEndTime = 231.3;
                    List list = (List) Objects.requireNonNull(document.getData()).get("sentence");

                    for (int i = 0; i < list.size(); i++) {
                        HashMap<String, Object> map = (HashMap) list.get(i);

                        // sentence 정보
                        String lyrics = (String) map.get("lyrics");
                        String startTime = (String) map.get("start_time");
                        String endTime = (String) map.get("end_time");

                        SentenceInfoDto sentenceInfoDto = SentenceInfoDto.builder()
                                .lyrics(lyrics)
                                .startTime(startTime)
                                .endTime(endTime)
                                .build();
                        sentenceInfoList.add(sentenceInfoDto);

                        // note 정보
                        ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");
                        for (HashMap<String, Object> notemap : arrayMap) {
                            SingingNoteDto noteDto = SingingNoteDto.builder()
                                    .startTime(String.valueOf(notemap.get("start_time")))
                                    .endTime(String.valueOf(notemap.get("end_time")))
                                    .note(String.valueOf(notemap.get("note")))
                                    .isNote((Boolean) notemap.get("isNote"))
                                    .build();

                            noteDtoList.add(noteDto);
                        }

                    }
                    settingView();

//                    } catch (Exception e) {
//                        Log.e("endTimeImport", e.getMessage());
//                    }
                }
            }
        });
    }
}