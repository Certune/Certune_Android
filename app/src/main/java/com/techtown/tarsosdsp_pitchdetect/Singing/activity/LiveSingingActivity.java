package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import static android.content.ContentValues.TAG;
import static com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx.noteToIdx;
import static com.techtown.tarsosdsp_pitchdetect.score.logics.CalcStartTimeRange.calcStartTimeRange;
import static com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessNoteRange.processNoteRange;
import static com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessTimeRange.processTimeRange;

import static java.lang.Math.abs;

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
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.okhttp.Dispatcher;
import com.techtown.tarsosdsp_pitchdetect.MyRecord.domain.UserWeakMusicDto;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SentenceInfoDto;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingMusicDto;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingNoteDto;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingUserScoreDto;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingUserUploadDto;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.global.SongSentenceDto;
import com.techtown.tarsosdsp_pitchdetect.score.domain.UserMusicDto;
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
import java.util.Objects;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.writer.WriterProcessor;

public class LiveSingingActivity extends AppCompatActivity {

    /* DB 접근용 */
    String userEmail;
    String songName;
    String singerName;
    Boolean isShifting;

    /* 계산용 변수 */
    // 곡 전체 변수
    ArrayList<SongSentenceDto> sentenceInfoList;

    // 소절용 변수
    private Double songEndTime;

    // 가사용
    ArrayList<String> lyricList;
    ArrayList<Double> startTimeList;
    ArrayList<Double> endTimeList;

    /* cell용 변수 */
    ArrayList<SentenceInfoDto> singingSentenceInfoList = new ArrayList<>();
    ArrayList<SingingNoteDto> singingNoteDtoList = new ArrayList<>();

    /* 유저 음정 정리 */
    String prevOctave;
    Map<Double, String> userMap; // {key : octav}
    double calcStartTime;

    /* 음악 재생 */
    String musicUrl;
    MediaPlayer mediaPlayer;

    /* shifting용 */
    String userLowKey;
    String userHighKey;
    int lowKeyIdx;
    int highKeyIdx;
    int midKeyIdx;
    ArrayList<Integer> midNoteIdxList = new ArrayList<>();
    ArrayList<Integer> shiftingList = new ArrayList<>();
    int shiftingIdx;


    /* TarsosDSP 및 파일 설정 */
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    AudioDispatcher dispatcher;
    File file;
    String filename = "singingResult.wav";


    /* 레이아웃 */
    // 기본 요소
    Button pitchGraph;

    private HorizontalScrollView scrollView;
    GridLayout gridLayout;
    Button cell;

    // 가사 변경
    final Handler timeHandler = new Handler();
    TextView currentLyric;
    TextView nextLyric;

    int firstNoteStartTime;
    Long showStartTime;

    // 로딩창
    LoadingDialog dialog;
    Boolean isNoteLoad = false;
    Boolean isMusicLoad = false;
    boolean isSongFirebaseLoad = false;
    boolean isSingingFirebaseLoad = false;

    // Firestore 설정
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    // cell 세팅
    final Handler handlerSong = new Handler();
    final Handler handlerRowSetting = new Handler();
    final Handler handlerColSetting = new Handler();


    final Runnable runnableSong = new Runnable() {
        @Override
        public void run() {
            Log.e("runnableSong", "얍");
            for (SingingNoteDto noteDto : singingNoteDtoList) {
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

    final Runnable runnableRow = new Runnable() {
        @Override
        public void run() {
            Log.e("runnableRow", "얍");
            for (int j = 0; j < gridLayout.getRowCount(); j++) {
                setCell(j, 0, 1, false, true);
            }
        }
    };

    final Runnable runnableCol = new Runnable() {
        @Override
        public void run() {
            Log.e("runnableCol", "얍");
            for (int i = 0; i < gridLayout.getColumnCount(); i++) {
                setCell(0, i, i + 1, false, true);
            }
        }
    };

    int tmp = 0;
    long duration = 1;
    final Runnable runnableLyric = new Runnable() {
        @Override
        public void run() {
            currentLyric.setText(lyricList.get(tmp));
            nextLyric.setText(lyricList.get(tmp+1));
            duration = (long) ((endTimeList.get(tmp) - startTimeList.get(tmp)) * 1000 - 95);
            Log.v("lyric ***", lyricList.get(tmp));
            if (tmp < (startTimeList.size() - 2)) tmp++;
            timeHandler.postDelayed(this, duration);
        }
    };


   // Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_singing);

        // intent 설정
        Intent subIntent = getIntent();
        songName = subIntent.getStringExtra("songName");
        singerName = subIntent.getStringExtra("singerName");
        isShifting = subIntent.getBooleanExtra("isShifting", false);
        shiftingIdx = subIntent.getIntExtra("shiftingIdx", 0);

        Log.v("livesinging", String.valueOf(shiftingIdx));
        handlerRowSetting.post(runnableRow);
        handlerColSetting.post(runnableCol);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            userEmail = user.getEmail();

        // layout 설정
        scrollView = findViewById(R.id.horizontalScrollView);
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setUseDefaultMargins(false);
        pitchGraph = findViewById(R.id.pitchGraph);

        currentLyric = findViewById(R.id.currentLyricTextView);
        nextLyric = findViewById(R.id.nextLyricTextView);

        dialog = new LoadingDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

//        stopButton = findViewById(R.id.stopButton);
//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopPitchDetection();
//            }
//        });

        // url 설정
        fetchAudioUrlFromFirebase();

        // tarsosDSP 관련 설정
        //setTarsosDSPSettings();

        File sdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        file = new File(sdCard, filename);

        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        // db로부터 점수 계산용 song info 가져오기
        getSentenceInfo();

        // graph 용
        microphoneOn();
    }

    private void findUserKeyInfo() {
        DocumentReference docRef = database.collection("User").document(userEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userLowKey = (String) document.getData().get("minUserPitch");
                        userHighKey = (String) document.getData().get("maxUserPitch");

                        lowKeyIdx = noteToIdx(userLowKey);
                        highKeyIdx = noteToIdx(userLowKey);
                        midKeyIdx = (lowKeyIdx + highKeyIdx) / 2;
                        compareMidNote();

                    } else {
                        Log.e("유저 정보 오류", "사용자 정보가 존재하지 않습니다.");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void compareMidNote() {
        database.collection("Singing").document(songName).collection("shifting")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        shiftingList.add(Integer.parseInt(document.getId()));
                        String midNoteIdx = document.getData().get("midKey").toString();
                        midNoteIdxList.add(Integer.parseInt(midNoteIdx));
                    }

                    shiftingIdx = 0;
                    int minVal = abs(midNoteIdxList.get(0) - midKeyIdx);
                    for (int i=0; i<shiftingList.size(); i++){
                        int curVal = abs(midNoteIdxList.get(i) - midKeyIdx);
                        if (curVal < minVal) {
                            minVal = curVal;
                            shiftingIdx = shiftingList.get(i);
                        }
                    }
                    Log.v("맞춤 note shift", String.valueOf(shiftingIdx));
                }
                else {
                    Log.d("TAG", "Error getting documents");
                }
            }
        });
    }

    private void setTarsosDSPSettings(){
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

    private ArrayList<NoteDto> sortMapKey(Object[] mapkey) {
        ArrayList<NoteDto> userKeyList = new ArrayList();
        String keyStartTime = mapkey[0].toString();
        String keyEndTime = "0.0";

        Object key;
        String keyValue;

        for (int i = 1; i < mapkey.length; i++) {
            // 이전 값을 넣어줌
            key = mapkey[i];
            keyValue = userMap.get(key);

            keyEndTime = key.toString();
            NoteDto keyNoteDto = NoteDto.builder()
                    .startTime(keyStartTime)
                    .note(keyValue)
                    .endTime(keyEndTime)
                    .build();
            userKeyList.add(keyNoteDto);
            // 현재 key의 시작 시간 update
            keyStartTime = key.toString();
        }

        key = mapkey[mapkey.length - 1];
        keyValue = userMap.get(key);
        NoteDto keyNoteDto = NoteDto.builder()
                .startTime(keyStartTime)
                .note(keyValue)
                .endTime(String.valueOf(Double.parseDouble(keyStartTime) + 0.05))
                .build();
        userKeyList.add(keyNoteDto);


        return userKeyList;
    }

    private void addDataToFireStore(ArrayList<NoteDto> sortedMapKey) {
        ArrayList<UserWeakMusicDto> userMusicInfo = checkUserDataIsInRange(sortedMapKey);
        SingingUserScoreDto singingUserScoreDto = calcUserScore(userMusicInfo);
        Intent intent = new Intent(getApplicationContext(), SingingResult.class);
        intent.putExtra("songName", songName);
        intent.putExtra("singerName", singerName);
        intent.putExtra("noteScore", Double.parseDouble(singingUserScoreDto.getNoteScore()));
        intent.putExtra("rhythmScore", Double.parseDouble(singingUserScoreDto.getRhythmScore()));
        intent.putExtra("totalScore", Double.parseDouble(singingUserScoreDto.getTotalScore()));
        startActivity(intent);
    }

    private ArrayList<UserWeakMusicDto> checkUserDataIsInRange(ArrayList<NoteDto> sortedMapKey) {

        ArrayList<UserWeakMusicDto> userNoteList = new ArrayList<>();

        int sentenceIdx = 0;
        double sentenceFirstNoteStartTime = sentenceInfoList.get(0).getSentenceStartTime();
        double sentenceStartTime = sentenceInfoList.get(0).getSentenceStartTime();
        double sentenceNoteEndTime = sentenceInfoList.get(0).getSentenceNoteEndTime();

        ArrayList<NoteDto> noteList = new ArrayList<>();
        boolean isFinal = false;

        Log.v("sentenceInfo", "sentenceStartTime : "+sentenceStartTime+ " sentenceEndTime : "+sentenceNoteEndTime);
        for (NoteDto noteDto : sortedMapKey) {
            double noteDtoEndTime = Double.parseDouble(noteDto.getEndTime());
            double noteDtoStartTime = Double.parseDouble(noteDto.getStartTime());
            Log.v("noteInfo", "noteDtoStartTime : "+noteDtoStartTime+ " noteDtoEndTime : "+noteDtoEndTime);
            // song 범위를 벗어난 note는 처리하지 않는다
            if (noteDtoEndTime >= songEndTime)
                break;
            // 첫 번째 sentence 전에 있는 note는 처리하지 않는다
            if (noteDtoStartTime < sentenceFirstNoteStartTime)
                continue;

            if (noteDtoStartTime > sentenceNoteEndTime
                    || noteDtoEndTime > sentenceNoteEndTime){
                // 현재 소절 정보 저장
                UserWeakMusicDto userMusicDto = UserWeakMusicDto.builder()
                        .startTime(String.valueOf(sentenceStartTime))
                        .notes(noteList)
                        .noteScore("null")
                        .rhythmScore("null")
                        .totalScore("null")
                        .build();
                userNoteList.add(userMusicDto);

                Log.v("userMusicDto", "================ "+userNoteList.size()+" ====================");
                for (int i=0; i<noteList.size(); i++)
                    Log.v("noteList", noteList.get(i).getNote() + " : " +noteList.get(i).getStartTime() + " : " + noteList.get(i).getEndTime());

                Log.v("sentenceInfo", "sentenceStartTime : "+sentenceStartTime+ " sentenceEndTime : "+sentenceNoteEndTime);
                // 다음 소절 정보로 update
                noteList = new ArrayList<>();
                sentenceIdx++;
                if (sentenceIdx >= sentenceInfoList.size()) {
                    isFinal = true;
                    break;
                }
                else {
                    sentenceStartTime = sentenceInfoList.get(sentenceIdx).getSentenceStartTime();
                    sentenceNoteEndTime = sentenceInfoList.get(sentenceIdx).getSentenceNoteEndTime();
                }
            }
            if (isFinal)
                break;
            noteList.add(noteDto);
        }
        return userNoteList;
    }

    private SingingUserScoreDto calcUserScore(ArrayList<UserWeakMusicDto> userMusicInfo) {

        int userTotalCorrectNoteNum = 0;
        int songTotalCorrectNoteNum = 0;
        double userTotalSentenceTime = 0;
        double songTotalSentenceTime = 0;

        ArrayList<String> weakSentenceIdxList = new ArrayList<>();
        ArrayList<SingingMusicDto> userMusicInfoList = new ArrayList<>();

        int sentenceIdx = 0;
        DecimalFormat df = new DecimalFormat("0.00");
        // [sentence별]
        for (UserWeakMusicDto userMusicDto : userMusicInfo){
            int noteIdx = 0;
            int userSentenceCorrectNoteNum = 0;
            double userSentenceTotalTime = 0.0;
            ArrayList<NoteDto> userNoteList = userMusicDto.getNotes();

            double userNoteStartTime = 0.0;

            boolean isWrongNote = false;
            boolean isFirstNote = false;

            // song sentence 정보
            SongSentenceDto sentenceInfoDto = sentenceInfoList.get(sentenceIdx);
            double sentenceStartTime = sentenceInfoDto.getSentenceStartTime();
            double sentenceEndTime = sentenceInfoDto.getSentenceEndTime();
            double sentenceFinalNoteEndTime = sentenceInfoDto.getSentenceNoteEndTime();
            double sentenceDurationTime = sentenceInfoDto.getSentenceDurationTime();
            ArrayList<NoteDto> sentenceNoteList = sentenceInfoDto.getSentenceNoteDtoList();

            double sentenceNoteEndTime = Double.parseDouble(sentenceNoteList.get(noteIdx).getEndTime());

            for (NoteDto userNoteDto : userNoteList) {
                userNoteStartTime = Double.parseDouble(userNoteDto.getStartTime());
                ArrayList<Double> timeRangeList = processTimeRange(sentenceNoteList.get(noteIdx).getStartTime());

                if (sentenceNoteEndTime <= userNoteStartTime){
                    noteIdx++;
                    sentenceNoteEndTime = Double.parseDouble(sentenceNoteList.get(noteIdx).getEndTime());

                    timeRangeList = processTimeRange(sentenceNoteList.get(noteIdx).getStartTime());
                    isFirstNote = false;
                }
                if (!isFirstNote && calcStartTimeRange(timeRangeList, userNoteDto.getStartTime())) {
                    userSentenceCorrectNoteNum++;
                    isFirstNote = true;
                }
                ArrayList<String> noteRangeList = processNoteRange(sentenceNoteList.get(noteIdx).getNote());
                if (noteRangeList.contains(userNoteDto.getNote())) {
                    userSentenceTotalTime += Double.parseDouble(userNoteDto.getEndTime()) - Double.parseDouble(userNoteDto.getStartTime());
                }
            }
            userTotalCorrectNoteNum += userSentenceCorrectNoteNum;
            songTotalCorrectNoteNum += sentenceNoteList.size();
            userTotalSentenceTime += userSentenceTotalTime;
            songTotalSentenceTime += sentenceDurationTime;

            double sentenceNoteScore = (userSentenceTotalTime / sentenceDurationTime);
            double sentenceRhythmScore = ((double) userSentenceCorrectNoteNum / sentenceNoteList.size());

            boolean isPoor = false;
            // 취약소절 여부 판정
            if (sentenceNoteScore <= 70 || sentenceRhythmScore <= 70){
                isPoor = true;
                weakSentenceIdxList.add(String.valueOf(sentenceIdx));
            }

            SingingMusicDto userUploadMusicDto = SingingMusicDto.builder()
                    .noteScore(df.format(sentenceNoteScore))
                    .rhythmScore(df.format(sentenceRhythmScore))
                    .totalScore(df.format((sentenceNoteScore + sentenceRhythmScore)/2))
                    .isPoor(isPoor)
                    .notes(userMusicDto.getNotes())
                    .build();


            Log.v("소절별 점수 산출", "==========="+userMusicInfoList.size()+"====================");
            Log.v("음정 점수", userUploadMusicDto.getNoteScore());
            Log.v("박자 점수", userUploadMusicDto.getRhythmScore());
            Log.v("노트 리스트", "=====");
            for (NoteDto noteDto : userUploadMusicDto.getNote()){
                Log.v("노트 정보", noteDto.getStartTime() + " : " + noteDto.getNote() + " : "+ noteDto.getEndTime());
            }
            userMusicInfoList.add(userUploadMusicDto);
            sentenceIdx++;
        }

        // UPLOAD LOGIC
        double totalNoteScore = (userTotalSentenceTime / songTotalSentenceTime) * 100;
        double totalRhythmScore = ((double) userTotalCorrectNoteNum / songTotalCorrectNoteNum) * 100;
        double totalScore = (totalNoteScore + totalRhythmScore) / 2;

        SingingUserScoreDto singingUserScoreDto = SingingUserScoreDto
                .builder()
                .noteScore(df.format(totalNoteScore))
                .rhythmScore(df.format(totalRhythmScore))
                .totalScore(df.format(totalScore))
                .build();

        Log.v("SCORE", totalNoteScore + "/" + totalRhythmScore + "/" + totalScore);
        uploadUserScore(userMusicInfoList, singingUserScoreDto);
        uploadWeakSentenceList(weakSentenceIdxList);
        return singingUserScoreDto;
    }

    private void uploadUserScore(ArrayList<SingingMusicDto> userMusicInfo, SingingUserScoreDto singingUserScoreDto) {
        SingingUserUploadDto singingUserUploadDto = SingingUserUploadDto.builder()
                .result(userMusicInfo)
                .singerName(singerName)
                .totalScore(singingUserScoreDto.getTotalScore())
                .noteScore(singingUserScoreDto.getNoteScore())
                .rhythmScore(singingUserScoreDto.getRhythmScore())
                .build();

        database.collection("User").document(userEmail).collection("userSongList").document("신호등")
                .set(singingUserUploadDto)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.v("음정 점수 산출 성공", "success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("음정 점수 산출 실패", "failed");
                    }
                });
    }

    private void uploadWeakSentenceList(ArrayList<String> weakSentenceIdxList) {
        Map<String, ArrayList<String>> userWeakSentenceMap = new HashMap<>();
        userWeakSentenceMap.put("weakSentence", weakSentenceIdxList);

        database.collection("User").document(userEmail).collection("userWeakSentenceList").document(songName)
                .set(userWeakSentenceMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.v("취약 소절 산출 성공", "success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("취약 소절 산출 실패", "failed");
                    }
                });
    }

    private void addWAVToFireStorage() {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        StorageReference filepath = mStorage.child("User").child(userEmail).child("songs").child(songName).child(filename + ".wav");

        // TODO : 장고 서버 연결
        Uri uri = Uri.fromFile(file);
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v("wav", "upload success");
            }
        });
    }

    public void createMediaPlayer(){
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

    // stream music directly from firebase
    private void fetchAudioUrlFromFirebase() {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef;
        if (isShifting && shiftingIdx != 0)
            storageRef = storage.child("songs").child(songName).child("mr_"+(shiftingIdx+4)+".mp3");
        else
            storageRef = storage.child("songs").child(songName).child("song.mp3");

        Log.v("db에서mr가져오기", "얍");
        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.v("진입 성공", "얍");
                        musicUrl = uri.toString();
                        Log.v("url", musicUrl);
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

    /* DB로부터 점수 계산에 필요한 점수를 가져옴 */
    private void getSentenceInfo() {
        DocumentReference docRef;
        if (isShifting && shiftingIdx != 0)
            docRef = database.collection("Shifting").document(songName).collection("songShifting").document(String.valueOf(shiftingIdx));
        else
            docRef = database.collection("Song").document(songName);
        docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        try {
                            songEndTime = Double.parseDouble(String.valueOf(document.getData().get("endTime")));
                            List list = (List) Objects.requireNonNull(document.getData()).get("sentence");
                            startTimeList = new ArrayList<>();
                            endTimeList = new ArrayList<>();
                            lyricList = new ArrayList<>();
                            sentenceInfoList = new ArrayList<>();
                            for (int i = 0; i < Objects.requireNonNull(list).size(); i++) {
                                HashMap<String, ArrayList<HashMap<String, Object>>> map = (HashMap) list.get(i);
                                ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");

                                double sentenceTotalTime = 0;
                                double sentenceNoteEndTime = 0;

                                // [Note] 정보
                                ArrayList<NoteDto> sentenceNoteDtoList = new ArrayList<>();
                                for (HashMap<String, Object> notemap : arrayMap) {
                                    NoteDto noteDto = NoteDto.builder()
                                            .startTime(String.valueOf(notemap.get("start_time")))
                                            .note(String.valueOf(notemap.get("note")))
                                            .endTime(String.valueOf(notemap.get("end_time")))
                                            .build();
                                    sentenceNoteDtoList.add(noteDto);
                                    sentenceTotalTime += Double.parseDouble(noteDto.getEndTime()) - Double.parseDouble(noteDto.getStartTime());
                                    sentenceNoteEndTime = Double.parseDouble(noteDto.getEndTime());
                                }

                                // [Sentence] 정보
                                double sentenceStartTime = Double.parseDouble(String.valueOf(map.get("start_time")));
                                double sentenceEndTime = Double.parseDouble(String.valueOf(map.get("end_time")));

                                SongSentenceDto songSentenceDto = SongSentenceDto.builder()
                                        .sentenceStartTime(sentenceStartTime)
                                        .sentenceDurationTime(sentenceTotalTime)
                                        .sentenceNoteEndTime(sentenceNoteEndTime)
                                        .sentenceEndTime(sentenceEndTime)
                                        .sentenceNoteDtoList(sentenceNoteDtoList)
                                        .sentenceNoteNum(sentenceNoteDtoList.size())
                                        .build();

                                // ArrayList에 소절별 시작 시간과 끝 시간 담기
                                startTimeList.add(sentenceStartTime);
                                endTimeList.add(sentenceEndTime);
                                lyricList.add(String.valueOf(map.get("lyrics")));

                                sentenceInfoList.add(songSentenceDto);
                            }
                            isSongFirebaseLoad = true;
                            checkFirebaseLoad();
                            getSingingInfo();
                        } catch (Exception e) {
                            Log.v("ERROR", "not enough records for calculating");
                        }
                    }
                }
        );
    }

    private void getSingingInfo() {

        DocumentReference docRef;
        if (isShifting && shiftingIdx != 0)
            docRef = database.collection("Singing").document(songName).collection("shifting").document(String.valueOf(shiftingIdx));
        else
            docRef = database.collection("Singing").document(songName);
        docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        List list = (List) (document.getData().get("sentence"));
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
                            singingSentenceInfoList.add(sentenceInfoDto);

                            // note 정보
                            ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");
                            for (HashMap<String, Object> notemap : arrayMap) {
                                SingingNoteDto noteDto = SingingNoteDto.builder()
                                        .startTime(String.valueOf(notemap.get("start_time")))
                                        .endTime(String.valueOf(notemap.get("end_time")))
                                        .note(String.valueOf(notemap.get("note")))
                                        .isNote((Boolean) notemap.get("isNote"))
                                        .build();

                                singingNoteDtoList.add(noteDto);
                            }
                        }
                        isSingingFirebaseLoad = true;
                        checkFirebaseLoad();
                    } catch (Exception e) {
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

    public void checkLoading() {
        Log.v("로딩완?", isNoteLoad + " : " + isMusicLoad);
        if (isNoteLoad && isMusicLoad) {
            dialog.dismiss();
            mediaPlayer.start();
            startPitchDetection();

            final Handler handlerStart = new Handler();

            handlerStart.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setScrollSettings();
                }
            }, firstNoteStartTime * 1000L);
        }
    }

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
                            if (!prevOctave.equals(octav) && !octav.equals("Nope")) {
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
        }
        else {
            cell.setBackgroundColor(Color.parseColor("#212121")); // 이거 없애면 버튼 홀쭉해짐
            Log.v("FALSE일 때 색깔", "얍");
        }
        if (isPrepare)
            cell.setBackgroundColor(Color.parseColor("#212121"));

        gridLayout.addView(cell, param);
    }

    public void settingView() {
        gridLayout.setColumnCount((int) Math.round(songEndTime * 10));

        Log.v("row 개수", String.valueOf(gridLayout.getRowCount()));
        Log.v("column개수", String.valueOf(gridLayout.getColumnCount()));

        // row, col 그려놓기
        handlerSong.post(runnableSong);
        timeHandler.postDelayed(runnableLyric, (long) (endTimeList.get(0) * 1000) - 1000);
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

    public void microphoneOn() {
        releaseDispatcher();

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        PitchDetectionHandler pitchDetectionHandler = (res, e) -> {
            final float pitchInHz = res.getPitch();
            String note = ProcessPitch.processPitch(pitchInHz);
            runOnUiThread(() -> {
//                pitchGraph.setY(1000 - pitchInHz);
                pitchGraph.setY((float) (1600 - pitchInHz * 3.4));
            });
        };

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }

    public void releaseDispatcher() {
        if (dispatcher != null) {
            if (!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
    }

}
