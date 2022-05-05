package com.techtown.tarsosdsp_pitchdetect;

import static android.content.ContentValues.TAG;
import static com.techtown.tarsosdsp_pitchdetect.score.CalcStartTimeRange.calcStartTimeRange;
import static com.techtown.tarsosdsp_pitchdetect.score.ProcessNoteRange.processNoteRange;
import static com.techtown.tarsosdsp_pitchdetect.score.ProcessTimeRange.processTimeRange;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techtown.tarsosdsp_pitchdetect.domain.MusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.SongTotalNoteTimeDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserMusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserNoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserSongInfoDto;
import com.techtown.tarsosdsp_pitchdetect.domain.SongSentenceDto;
import com.techtown.tarsosdsp_pitchdetect.score.ProcessPitch;

import java.io.File;
import java.io.FileInputStream;
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
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.writer.WriterProcessor;
import lombok.SneakyThrows;

public class SingingActivity extends AppCompatActivity {
    // 로그인된 유저의 이름, 이메일, uid 정보
    String userName;
    String userEmail;
    String userSex;
    String uid;

    String songName;
    String singerName;
    Boolean isShifting;

    String songUrl;

    private TextView displayName;

    Map<Double, String> map; // {key : octav}
    Map<Double, String> musicMap;

    ArrayList<Double> startTimeList;
    ArrayList<Double> endTimeList;
    ArrayList<MusicDto> musicTotalInfoList;

    AudioDispatcher dispatcher;
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    MediaPlayer mediaPlayer;

    File file;

    TextView pitchTextView;
    Button recordButton;
    Button playButton;

    boolean isRecording = false;
    String filename = "recorded_sound.wav";

    // firebase db 연동
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent subIntent = getIntent();
        userEmail = subIntent.getStringExtra("userEmail");
        songName = subIntent.getStringExtra("songName");
        singerName = subIntent.getStringExtra("singerName");
        isShifting = subIntent.getBooleanExtra("isShifting", false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            userEmail = user.getEmail();

            DocumentReference docRef = database.collection("User").document(userEmail);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            userName = (String) document.getData().get("name");
                            userSex = (String) document.getData().get("sex");

                            displayName = (TextView) findViewById(R.id.displayName);
                            displayName.setText(userName);
                        } else {
                            Log.d(TAG, "사용자 정보가 존재하지 않습니다.");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });


            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            uid = user.getUid();
        }

        File sdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        file = new File(sdCard, filename);

        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

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

        pitchTextView = findViewById(R.id.pitchTextView);
        recordButton = findViewById(R.id.recordButton);
        playButton = findViewById(R.id.playButton);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    recordAudio();
                    isRecording = true;
                    recordButton.setText("중지");
                } else {
                    stopRecording();
                    isRecording = false;
                    recordButton.setText("녹음");
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });
    }

    // stream music directly from firebase
    private void fetchAudioUrlFromFirebase() {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        StorageReference filepath = mStorage.child("songs").child("Traffic_Light.mp3");
        Log.v("FILEPATH", filepath.toString());

        filepath.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            Log.v("download url", uri.toString());
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(uri.toString());
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    Log.v("준비완", "간다");
                                    mp.start();
                                }
                            });
                            mediaPlayer.prepare();
                        } catch (Exception e) {
                            Log.e("FETCH MUSIC", e.toString());
                        }
                    }
                });
    }

    public void playAudio() {
        musicMap = new HashMap<>(); // 녹음될 때마다 사용자 음성 담은 map 초기화
        long start = System.nanoTime(); // 시작 시간 측정
        try {
            releaseDispatcher();

            // 성공 ) 얘는 dispatcher와 별개로 돌아가는 메소드
            //mediaPlayer = MediaPlayer.create(this, R.raw.music);
            //mediaPlayer.start();

            FileInputStream fileInputStream = new FileInputStream(file);
            // 마이크가 아닌 파일을 소스로 하는 dispatcher 생성 -> AudioDispatcher 객체 생성 시 UniversalAudioInputStream 사용
            dispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);

            AudioProcessor playerProcessor = new AndroidAudioPlayer(tarsosDSPAudioFormat, 2048, 0);
            dispatcher.addAudioProcessor(playerProcessor);

            PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                    final float pitchInHz = res.getPitch();
                    String octav = ProcessPitch.processPitch(pitchInHz);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pitchTextView.setText(octav);
                            long end = System.nanoTime();
                            double time = (end - start) / (1000000000.0);

                            if (!octav.equals("Nope")) {// 의미있는 값일 때만 입력받음
                                Log.v("time", String.valueOf(time));
                                musicMap.put(time, octav);
                            }
                        }
                    });
                }
            };

            AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
            dispatcher.addAudioProcessor(pitchProcessor);

            Thread audioThread = new Thread(dispatcher, "Audio Thread");
            audioThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String prevOctave;

    public void recordAudio() {
        fetchAudioUrlFromFirebase();
        prevOctave = "";
        map = new HashMap<>(); // 녹음될 때마다 map 초기화
        long start = System.nanoTime(); // 시작 시간 측정

        Log.v("start", "start time measuring process");
        releaseDispatcher();
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        try {
            Log.v("start2", "try문");
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
                            pitchTextView.setText(octav);
                            long end = System.nanoTime();
                            double time = (end - start) / (1000000000.0);

                            if (!octav.equals("Nope")) { // 의미있는 값일 때만 입력받음
                                Log.v("time", String.valueOf(time));
                                if (!prevOctave.equals(octav)) {
                                    Log.v("time / octave", String.valueOf(time) + " / " + octav);
                                    map.put(time, octav);
                                    prevOctave = octav;
                                }
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

    public void stopRecording() {

        Object[] mapkey = map.keySet().toArray();
        Arrays.sort(mapkey);
        for (Object key : mapkey) {
            Log.v("result", String.valueOf(key) + "/ value: " + map.get(key));
        }

        addWAVToFireStorage();
        addDataToFireStore(mapkey);

        releaseDispatcher();
    }

    public void releaseDispatcher() {
        if (dispatcher != null) {
            if (!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseDispatcher();
    }

    public void addDataToFireStore(Object[] mapkey) {

        ArrayList<UserMusicDto> userMusicInfoList = checkUserDataIsInRange(mapkey);
        for (UserMusicDto userMusicDto : userMusicInfoList) {
            Log.v("범위 확인 완료", userMusicDto.getStartTime());
        }
        // TODO : songName 반영
        calcUserScore(userMusicInfoList, "신호등");
    }

    public void addWAVToFireStorage() {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        StorageReference filepath = mStorage.child("Audio").child(filename);

        Uri uri = Uri.fromFile(file);
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v("wav", "upload success");
            }
        });
    }

    public ArrayList<UserMusicDto> checkUserDataIsInRange(Object[] mapkey) {
        int idx = 0;
        Double startTime = 0.0;
        Double nextStartTime;

        ArrayList<UserNoteDto> noteList = new ArrayList<>();
        ArrayList<UserMusicDto> sentenceList = new ArrayList<>();

        Log.v("START TIME LIST", String.valueOf(startTimeList));
        boolean flag = false;
        boolean isStart = false;
        for (Object key : mapkey) {
            try {
                startTime = startTimeList.get(idx);
                nextStartTime = endTimeList.get(idx);
            } catch (IndexOutOfBoundsException e) {
                // 다음 소절이 존재하지 않는 경우
                nextStartTime = 50.0;
            }
            // 소절이 시작한 뒤 입력된 음성만 처리
            if (startTimeList.get(0) <= Double.parseDouble(key.toString())) {
                isStart = true;
                if (nextStartTime > Double.parseDouble(key.toString())) {
                    flag = false;
                    // 다음 소절 전까지 noteList에 note 담음
                    noteList.add(new UserNoteDto(String.valueOf(key), map.get(key)));

                } else { // 다음 소절로 넘어갔을 때 이전 소절에 대한 처리
                    flag = true;
                    UserMusicDto userMusicDto = UserMusicDto.builder()
                            .startTime(String.valueOf(startTime))
                            .notes(noteList)
                            .noteScore("null")
                            .rhythmScore("null")
                            .totalScore("null")
                            .build();
                    sentenceList.add(userMusicDto);

                    idx++;

                    // 한 소절에 대한 처리가 끝난 후 noteList 초기화 및 직전에 들어온 값 add
                    noteList = new ArrayList<>();
                    noteList.add(new UserNoteDto(String.valueOf(key), map.get(key)));
                }

            }
        }

        if (!flag && isStart) {
            UserMusicDto userMusicDto = UserMusicDto.builder()
                    .startTime(String.valueOf(startTime))
                    .notes(noteList)
                    .noteScore("null")
                    .rhythmScore("null")
                    .totalScore("null")
                    .build();
            sentenceList.add(userMusicDto);
        }
        return sentenceList;
    }

    // TODO : 전역 변수로 songName 설정해야 함
    public void calcUserScore(ArrayList<UserMusicDto> userMusicInfoList, String songName) {

        int userTotalNoteNum = 0;
        double userTotalSentenceTime = 0;

        SongTotalNoteTimeDto songTotalNoteTimeDto = new SongTotalNoteTimeDto();
        List<SongSentenceDto> songSentenceInfoList = getSongSentenceInfo(songTotalNoteTimeDto);
        ArrayList<Integer> weakSentenceIdxList = new ArrayList<>();

        int sentenceIdx = 0;
        DecimalFormat df = new DecimalFormat("0.00");
        for (UserMusicDto userMusicDto : userMusicInfoList) {
            int noteIdx = 0;
            int userCorrectNoteNum = 0;

            SongSentenceDto songSentenceDto = songSentenceInfoList.get(sentenceIdx);
            ArrayList<NoteDto> sentenceNoteDtoList = songSentenceDto.getSentenceNoteDtoList();

            boolean isWrongNote = false;
            boolean isFirstNote = false;

            ArrayList<UserNoteDto> userNotes = userMusicDto.getNotes();
            Double userNoteStartTime = 0.0;
            Double userSentenceTime = songSentenceDto.getSentenceDurationTime();
            Double sentenceStartTime = songSentenceDto.getSentenceStartTime();
            Double sentenceEndTime = songSentenceDto.getSentenceEndTime();
            Double songNoteEndTime = Double.parseDouble(sentenceNoteDtoList.get(noteIdx).getEndTime());

            for (UserNoteDto userNoteDto : userNotes) {

                if (isWrongNote) {
                    userSentenceTime -= (Double.parseDouble(userNoteDto.getStartTime()) - userNoteStartTime);
                    isWrongNote = false;
                }

                userNoteStartTime = Double.parseDouble(userNoteDto.getStartTime());

                if (sentenceStartTime <= userNoteStartTime && sentenceEndTime >= userNoteStartTime) {
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
                    if (!noteRangeList.contains(userNoteDto.getNote()))
                        isWrongNote = true;
                }
            }
            if (isWrongNote) {
                userSentenceTime -= (sentenceEndTime - userNoteStartTime);
            }
            userTotalSentenceTime += userSentenceTime;
            userTotalNoteNum += userCorrectNoteNum;

            Double sentenceDurationTime = songSentenceDto.getSentenceDurationTime();
            int songSentenceNoteNum = songSentenceDto.getSentenceNoteNum();

            Double sentenceNoteScore = (userSentenceTime / sentenceDurationTime) * 100;
            Double sentenceRhythmScore = ((double) userCorrectNoteNum / songSentenceNoteNum) * 100;

            userMusicDto.setNoteScore(df.format(sentenceNoteScore));
            userMusicDto.setRhythmScore(df.format(sentenceRhythmScore));
            userMusicDto.setTotalScore(df.format((sentenceNoteScore + sentenceRhythmScore) / 2));

            if (sentenceNoteScore <= 70 || sentenceRhythmScore <= 70) {
                userMusicDto.setIsPoor(true);
                weakSentenceIdxList.add(sentenceIdx);
            }
            sentenceIdx++;
        }

        double totalNoteScore = ((double) userTotalSentenceTime / songTotalNoteTimeDto.getSongTotalSentenceTime()) * 100;
        double totalRhythmScore = ((double) userTotalNoteNum / songTotalNoteTimeDto.getSongTotalNoteNum()) * 100;
        double totalScore = (totalNoteScore + totalRhythmScore) / 2;

        UserSongInfoDto userSongInfoDto = UserSongInfoDto
                .builder()
                .songInfo(userMusicInfoList)
                .totalScore(df.format(totalScore))
                .noteScore(df.format(totalNoteScore))
                .rhythmScore(df.format(totalRhythmScore))
                .build();

        Log.v("SCORE", totalNoteScore + "/" + totalRhythmScore + "/" + totalScore);
        uploadUserScore(songName, userSongInfoDto);
        uploadWeakSentenceList(songName, weakSentenceIdxList);
    }

    private List<SongSentenceDto> getSongSentenceInfo(SongTotalNoteTimeDto songTotalNoteTimeDto) {
        List<SongSentenceDto> songSentenceInfoList = new ArrayList<>();
        for (MusicDto musicinfo : musicTotalInfoList) {

            double sentenceTime = Double.parseDouble(musicinfo.getEndTime()) - Double.parseDouble(musicinfo.getStartTime());
            ArrayList<NoteDto> musicNoteDtos = musicinfo.getNotes(); // 소절의 note 정보

            songTotalNoteTimeDto.addSongTotalSentenceTime(sentenceTime);
            songTotalNoteTimeDto.addSongTotalNoteNum(musicNoteDtos.size());

            SongSentenceDto songSentenceDto = SongSentenceDto.builder()
                    .sentenceStartTime(Double.parseDouble(musicinfo.getStartTime()))
                    .sentenceEndTime(Double.parseDouble(musicinfo.getEndTime()))
                    .sentenceNoteDtoList(musicNoteDtos)
                    .sentenceDurationTime(sentenceTime)
                    .sentenceNoteNum(musicNoteDtos.size())
                    .build();

            songSentenceInfoList.add(songSentenceDto);
        }
        return songSentenceInfoList;
    }

    private void uploadUserScore(String songName, UserSongInfoDto userSongInfoDto) {
        Map<String, UserSongInfoDto> userMusicList = new HashMap<>();
        userMusicList.put("sentence", userSongInfoDto);

        database.collection("User").document(userEmail).collection("userSongList").document("신호등")
                .set(userMusicList)
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

    private void uploadWeakSentenceList(String songName, ArrayList<Integer> weakSentenceIdxList) {
        Map<String, ArrayList<Integer>> userWeakSentenceMap = new HashMap<>();
        userWeakSentenceMap.put("weakSentence", weakSentenceIdxList);

        database.collection("User").document(userEmail).collection("userWeakSentenceList").document("신호등")
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
}