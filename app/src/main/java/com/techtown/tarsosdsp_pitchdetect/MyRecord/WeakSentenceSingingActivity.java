package com.techtown.tarsosdsp_pitchdetect.MyRecord;

import static com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx.noteToIdx;
import static com.techtown.tarsosdsp_pitchdetect.score.logics.CalcStartTimeRange.calcStartTimeRange;
import static com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessNoteRange.processNoteRange;
import static com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessTimeRange.processTimeRange;

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

import androidx.gridlayout.widget.GridLayout;

import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.domain.UserWeakMusicDto;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.Singing.activity.LoadingDialog;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingNoteDto;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingUserScoreDto;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessPitch;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.writer.WriterProcessor;

public class WeakSentenceSingingActivity extends AppCompatActivity {

    /* DB 접근용 */
    String songName;
    String sentenceIdx;
    String userEmail;

    String lyric;
    double sentenceStartTime;
    double sentenceEndTime;
    double sentenceNoteEndTime;
    List<NoteDto> sentenceNoteDtoList = new ArrayList<>();

    /* 레이아웃 */
    // 기본 요소
    HorizontalScrollView scrollView;
    GridLayout gridLayout;
    Button cell;
    TextView lyricText;
    List<SingingNoteDto> singingNoteDtoList = new ArrayList<>();

    // 로딩창
    LoadingDialog dialog;
    boolean isNoteLoad = false;
    boolean isMusicLoad = false;

    boolean isSongFirebaseLoad = false;
    boolean isSingingFirebaseLoad = false;

    /* 점수 산출 */
    String prevOctave;
    Map<Double, String> userMap;
    double calcStartTime;
    double sentenceTotalTime; // 쉼표를 제외한 sentence 전체 시간 -> db에서 불러올 때 계싼

    /* 음악 재생 */
    String musicUrl;
    MediaPlayer mediaPlayer;

    /* TarsosDSP 및 파일 설정 */
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    AudioDispatcher dispatcher;

    File file;
    String filename = "sentenceResult";

    // firestore
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    /* handler 설정 */
    final Handler handlerSong = new Handler();
    final Handler handlerRowSetting = new Handler();
    final Handler handlerColSetting = new Handler();


    final Runnable runnableSong = new Runnable() {
        @Override
        public void run() {
            for (SingingNoteDto noteDto : singingNoteDtoList) {
                int noteIdx = noteToIdx(noteDto.getNote());
                int startTime = (int) Math.round(Double.parseDouble(noteDto.getStartTime()) * 30);
                int endTime = (int) Math.round(Double.parseDouble(noteDto.getEndTime()) * 30);
                boolean isNote = noteDto.getIsNote();
                Log.v("isNote", String.valueOf(isNote));
                setCell(noteIdx, startTime, endTime, isNote, false);
            }
            isNoteLoad = true;
            checkLoading();
        }
    };

    final Runnable runnableRow = new Runnable() {
        @Override
        public void run() {
            for (int j = 0; j < gridLayout.getRowCount(); j++) {
                setCell(j, 0, 1, false, true);
            }
        }
    };

    final Runnable runnableCol = new Runnable() {
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
        setContentView(R.layout.activity_weak_sentence_singing);

        // intent 설정
        Intent subIntent = getIntent();
        songName = subIntent.getStringExtra("songName");
        sentenceIdx = subIntent.getStringExtra("sentenceIdx");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            userEmail = user.getEmail();

        // layout 설정
        scrollView = findViewById(R.id.horizontalScrollView_weakSentence);
        gridLayout = findViewById(R.id.gridLayout_weakSentence);
        lyricText = findViewById(R.id.currentLyricTextView_weakSentence);

        dialog = new LoadingDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        // url 설정
        fetchAudioUrlFromFirebase();

        // file 설정
        setTarsosDSPSettings();

        // db로부터 소절정보 가져오기
        getSentenceInfo();

    }

    public void checkLoading() {
        if (isNoteLoad && isMusicLoad) {
            dialog.dismiss();
            mediaPlayer.start();
            setScrollSettings();
            startPitchDetection();

            final Handler handlerStart = new Handler();

            handlerStart.postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            }, 1000L);
        }
    }

    private void setScrollSettings() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollX", 100000);
                int durationTime = Math.round(11 * 100000 / 20); // TODO : SONG 길이에 맞춰서 넣어줘야 함
                objectAnimator.setDuration(durationTime);
                objectAnimator.setInterpolator(new LinearInterpolator());
                objectAnimator.start();
            }
        });
    }

    public void setCell(int noteIdx, int startTime, int endTime, boolean isNote, boolean isPrepare) {
        Log.v("노트 인덱스", String.valueOf(noteIdx));
        Log.v("시작 시간", String.valueOf(startTime));
        Log.v("종료 시간", String.valueOf(endTime));
        androidx.gridlayout.widget.GridLayout.Spec cellRow = androidx.gridlayout.widget.GridLayout.spec(noteIdx, 1, 1f);
        androidx.gridlayout.widget.GridLayout.Spec cellCol = androidx.gridlayout.widget.GridLayout.spec(startTime);

        androidx.gridlayout.widget.GridLayout.LayoutParams param = new androidx.gridlayout.widget.GridLayout.LayoutParams(cellRow, cellCol);
        param.height = 0;
        param.width = (endTime - startTime) * 30;
        param.setGravity(Gravity.FILL_HORIZONTAL);

        cell = new Button(this);
        cell.setEnabled(false);
        cell.setLayoutParams(param);
        if (isNote) {
            cell.setBackgroundColor(Color.GRAY);
        } else {
            cell.setBackgroundColor(Color.parseColor("#212121")); // 이거 없애면 버튼 홀쭉해짐
        }
        if (isPrepare)
            cell.setBackgroundColor(Color.parseColor("#212121"));

        gridLayout.addView(cell, param);
    }

    public void settingView() {
        gridLayout.setColumnCount((int) Math.round((sentenceEndTime - sentenceStartTime) * 30));
        Log.v("row 개수", String.valueOf(gridLayout.getRowCount()));
        Log.v("column개수", String.valueOf(gridLayout.getColumnCount()));

        // row, col 그려놓기
        handlerRowSetting.post(runnableRow);
        handlerColSetting.post(runnableCol);
        handlerSong.post(runnableSong);
    }

    /* 점수 산출용 */
    private void startPitchDetection() {
        prevOctave = "";
        userMap = new HashMap<>(); // 노긍ㅁ할 때마다 사용자 음성 담은 map 초기화
        calcStartTime = System.nanoTime();
        Log.v("로딩 종료 시간", String.valueOf(calcStartTime));

        releaseDispatcher();
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            AudioProcessor recordProcessor = new WriterProcessor(tarsosDSPAudioFormat, randomAccessFile);
            dispatcher.addAudioProcessor(recordProcessor);

            PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                    final float pitchInHz = res.getPitch();
                    String octav = ProcessPitch.processPitch(pitchInHz);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long end = System.nanoTime();
                            double time = (end - calcStartTime) / (1000000000.0);

                            // 의미있는 값일 때만 입력받음
                            Log.v("time", String.valueOf(time));
                            if (!prevOctave.equals(octav)) {
                                Log.v("time / octave", String.valueOf(time) + " / " + octav);
                                userMap.put(time, octav);
                                prevOctave = octav;
                            }
                        }
                    });
                }
            };

            AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
            dispatcher.addAudioProcessor(pitchProcessor);

            Thread audioThread = new Thread(dispatcher, "Audio Thread");
            audioThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPitchDetection() {
        Object[] mapkey = userMap.keySet().toArray();
        Arrays.sort(mapkey);
        for (Object key : mapkey) {
            Log.v("result", String.valueOf(key) + "/ value: " + userMap.get(key));
        }
        ArrayList<NoteDto> sortedMapKey = sortMapKey(mapkey);
        for (NoteDto noteDto : sortedMapKey) {
            Log.v("sortedResult", noteDto.getStartTime() + " : " + noteDto.getNote() + " : " + noteDto.getEndTime());
        }
        addWAVToFireStorage();
        addDataToFireStore(sortedMapKey);

        releaseDispatcher();
    }

    public ArrayList<NoteDto> sortMapKey(Object[] mapkey) {
        ArrayList<NoteDto> userKeyList = new ArrayList();
        String keyStartTime = mapkey[0].toString();
        String keyEndTime = "0.0";

        Object key;
        String keyValue;

        for (int i = 1; i < mapkey.length; i++) {
            // 이전 값을 넣어줌
            key = mapkey[i];
            keyValue = userMap.get(key);

            if (!keyValue.equals("Nope")) {
                keyEndTime = key.toString();
                NoteDto keyNoteDto = NoteDto.builder()
                        .startTime(keyStartTime)
                        .note(keyValue)
                        .endTime(keyEndTime)
                        .build();
                userKeyList.add(keyNoteDto);
            }
            // 현재 key의 시작 시간 update
            keyStartTime = key.toString();
        }

        key = mapkey[mapkey.length - 1];
        keyValue = userMap.get(key);
        if (!keyValue.equals("Nope")) {
            NoteDto keyNoteDto = NoteDto.builder()
                    .startTime(keyStartTime)
                    .note(keyValue)
                    .endTime(keyStartTime + 0.05)
                    .build();
            userKeyList.add(keyNoteDto);
        }

        return userKeyList;
    }

    public SingingUserScoreDto calcUserScore(UserWeakMusicDto userMusicDto) {

        // 소절 내에 들어온 note들
        int noteIdx = 0;
        int userCorrectNoteNum = 0;
        boolean isFirstNote = false;

        ArrayList<NoteDto> userNotes = userMusicDto.getNotes();

        Double userTotalTime = 0.0;
        Double userNoteStartTime = 0.0;
        Double songNoteEndTime = Double.parseDouble(sentenceNoteDtoList.get(noteIdx).getEndTime());

        for (NoteDto userNoteDto : userNotes) {
            userNoteStartTime = Double.parseDouble(userNoteDto.getStartTime());
            ArrayList<Double> timeRangeList = processTimeRange(sentenceNoteDtoList.get(noteIdx).getStartTime());

            if (songNoteEndTime <= userNoteStartTime) {
                noteIdx++;
                songNoteEndTime = Double.parseDouble(sentenceNoteDtoList.get(noteIdx).getEndTime());

                timeRangeList = processTimeRange(sentenceNoteDtoList.get(noteIdx).getStartTime());
                isFirstNote = false;
            }
            if (!isFirstNote && calcStartTimeRange(timeRangeList, userNoteDto.getStartTime())) {
                userCorrectNoteNum++;
                isFirstNote = true;
            }

            ArrayList<String> noteRangeList = processNoteRange(sentenceNoteDtoList.get(noteIdx).getNote());
            if (noteRangeList.contains(userNoteDto.getNote())) {
                userTotalTime += Double.parseDouble(userNoteDto.getEndTime()) - Double.parseDouble(userNoteDto.getStartTime());
            }
        }

        DecimalFormat df = new DecimalFormat("0.00");

        double totalNoteScore = (userTotalTime / sentenceTotalTime) * 100;
        double totalRhythmScore = ((double) userCorrectNoteNum / sentenceNoteDtoList.size()) * 100;
        double totalScore = (totalNoteScore + totalRhythmScore) / 2;

        SingingUserScoreDto singingUserScoreDto = SingingUserScoreDto
                .builder()
                .noteScore(df.format(totalNoteScore))
                .rhythmScore(df.format(totalRhythmScore))
                .totalScore(df.format(totalScore))
                .build();

        Log.v("SCORE 상세", userTotalTime + "/" + sentenceTotalTime);
        Log.v("SCORE", totalNoteScore + "/" + totalRhythmScore + "/" + totalScore);
        return singingUserScoreDto;
    }

    public UserWeakMusicDto checkUserDataIsInRange(ArrayList<NoteDto> sortedMapKey) {

        ArrayList<NoteDto> userWeakNoteList = new ArrayList<>();

        for (NoteDto noteDto : sortedMapKey) {
            if (Double.parseDouble(noteDto.getEndTime()) > sentenceNoteEndTime)
                break;
            userWeakNoteList.add(noteDto);
        }

        UserWeakMusicDto userMusicDto = UserWeakMusicDto.builder()
                .startTime("0.0")
                .notes(userWeakNoteList)
                .noteScore("null")
                .rhythmScore("null")
                .totalScore("null")
                .build();

        return userMusicDto;
    }

    private void addDataToFireStore(ArrayList<NoteDto> sortedMapKey) {
        UserWeakMusicDto userMusicInfo = checkUserDataIsInRange(sortedMapKey);
        SingingUserScoreDto singingUserScoreDto = calcUserScore(userMusicInfo);
        Intent intent = new Intent(getApplicationContext(), WeakSentenceSingingResultActivity.class);
        intent.putExtra("noteScore", singingUserScoreDto.getNoteScore());
        intent.putExtra("rhythmScore", singingUserScoreDto.getRhythmScore());
        intent.putExtra("totalScore", singingUserScoreDto.getTotalScore());
        startActivity(intent);
    }

    private void addWAVToFireStorage() {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        StorageReference filepath = mStorage.child("User").child(userEmail).child("songs").child(songName).child(sentenceIdx + ".wav");

        Uri uri = Uri.fromFile(file);
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v("wav", "upload success");
            }
        });
    }

    public void releaseDispatcher() {
        if (dispatcher != null) {
            if (!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
    }

    /* DB 접근 */
    private void getSentenceInfo() {
        database.collection("Song").document(songName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                try {
                    ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) document.getData().get("sentence");

                    Log.v("songName", songName);
                    HashMap<String, Object> sentencemap = (HashMap<String, Object>) list.get(Integer.parseInt(sentenceIdx));
                    Log.v("sentence 번호", sentenceIdx);
                    lyric = (String) sentencemap.get("lyrics");
                    sentenceEndTime = Double.parseDouble(String.valueOf(sentencemap.get("end_time")));
                    sentenceStartTime = Double.parseDouble(String.valueOf(sentencemap.get("start_time")));
                    Log.v("가사 확인", lyric);
                    Log.v("시작 시간", String.valueOf(sentenceStartTime));
                    Log.v("종료 시간", String.valueOf(sentenceEndTime));

                    lyricText.setText(lyric);

                    ArrayList<HashMap<String, Object>> noteArrayMap = (ArrayList<HashMap<String, Object>>) sentencemap.get("notes");
                    for (HashMap<String, Object> noteMap : noteArrayMap) {
                        NoteDto noteDto = NoteDto.builder()
                                .startTime(String.valueOf(Double.parseDouble((String) noteMap.get("start_time")) - sentenceStartTime))
                                .endTime(String.valueOf(Double.parseDouble((String) noteMap.get("end_time")) - sentenceStartTime))
                                .note(String.valueOf(noteMap.get("note")))
                                .build();

                        Log.v("SONG 정보", noteDto.getStartTime() + " : " + noteDto.getEndTime() + " : " + noteDto.getNote());
                        sentenceNoteDtoList.add(noteDto);
                        sentenceTotalTime += Double.parseDouble(noteDto.getEndTime()) - Double.parseDouble(noteDto.getStartTime());
                        sentenceNoteEndTime = Double.parseDouble(noteDto.getEndTime());
                    }
                    isSongFirebaseLoad = true;
                    checkFirebaseLoad();
                    getSingingInfo();
                } catch (Exception e) {
                    Log.e("getSentenceInfo", "해당 소절의 정보를 가져올 수 없습니다.");
                }
            }
        });
    }

    private void getSingingInfo() {
        database.collection("Singing").document(songName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                try {
                    List list = (List) document.getData().get("sentence");
                    HashMap<String, Object> sentencemap = (HashMap<String, Object>) list.get(Integer.parseInt(sentenceIdx) + 1);
                    ArrayList<HashMap<String, Object>> noteArrayMap = (ArrayList<HashMap<String, Object>>) sentencemap.get("notes");
                    for (HashMap<String, Object> noteMap : noteArrayMap) {
                        SingingNoteDto singingNoteDto = SingingNoteDto.builder()
                                .startTime(String.valueOf(Double.parseDouble((String) noteMap.get("start_time")) - sentenceStartTime))
                                .endTime(String.valueOf(Double.parseDouble((String) noteMap.get("end_time")) - sentenceStartTime))
                                .note(String.valueOf(noteMap.get("note")))
                                .isNote((Boolean) noteMap.get("isNote"))
                                .build();

                        Log.v("이것이시작시간", singingNoteDto.getStartTime());

                        singingNoteDtoList.add(singingNoteDto);
                    }
                    isSingingFirebaseLoad = true;
                    checkFirebaseLoad();
                } catch (Exception e) {
                    Log.e("getSingingInfo", "해당 소절의 정보를 가져올 수 없습니다.");
                    Log.e("getSingingInfo", e.getMessage());
                }
            }
        });
    }

    private void checkFirebaseLoad() {
        Log.v("db 로딩", isSongFirebaseLoad + " : " + isSingingFirebaseLoad);
        if (isSongFirebaseLoad && isSingingFirebaseLoad) {
            settingView();
        }
    }

    private void setTarsosDSPSettings() {
        File sdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        file = new File(sdCard, filename);

        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));
    }

    /* Mediaplayer 설정 */
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
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPitchDetection();
                }
            });
        } catch (Exception e) {
            Log.e("MEDIAPLAYER", e.getMessage());
        }
    }

    private void fetchAudioUrlFromFirebase() {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child("songs").child(songName).child(sentenceIdx + ".mp3");

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
}