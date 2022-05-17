package com.techtown.tarsosdsp_pitchdetect;

import static com.techtown.tarsosdsp_pitchdetect.score.CalcStartTimeRange.calcStartTimeRange;
import static com.techtown.tarsosdsp_pitchdetect.score.ProcessNoteRange.processNoteRange;
import static com.techtown.tarsosdsp_pitchdetect.score.ProcessTimeRange.processTimeRange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
<<<<<<< Updated upstream:app/src/main/java/com/techtown/tarsosdsp_pitchdetect/OctaveTestSingingActivity.java
import com.techtown.tarsosdsp_pitchdetect.domain.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.TestMusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserMusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserNoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserSongInfoDto;
import com.techtown.tarsosdsp_pitchdetect.score.ProcessPitch;
=======
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.OctaveTest.domain.TestMusicInfoDto;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.score.domain.UserMusicDto;
import com.techtown.tarsosdsp_pitchdetect.score.domain.UserNoteDto;
import com.techtown.tarsosdsp_pitchdetect.OctaveTest.domain.TestUserResultDto;
import com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessPitch;
>>>>>>> Stashed changes:app/src/main/java/com/techtown/tarsosdsp_pitchdetect/OctaveTest/activity/TestSingingActivity.java

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

public class OctaveTestSingingActivity extends AppCompatActivity {

    private String userEmail;
    private String userSex;
    private String octaveHighLow;

    TextView pitchTextView;
    Button recordButton;
    Button stopButton;

    Map<Double, String> map;
    String musicUrl;
    long musicStartTime;

    ArrayList<Double> startTimeList;
    ArrayList<Double> endTimeList;
    ArrayList<TestMusicDto> musicTotalInfoList;

    AudioDispatcher dispatcher;
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    MediaPlayer mediaPlayer;

    File file;

    boolean isRecording = false;
    String filename = "user_octave_test";

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_octave_test_singing);

        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");
        userSex = intent.getStringExtra("userSex");
        octaveHighLow = intent.getStringExtra("octaveHighLow");

        Log.v("USEREMAIL", userEmail);
        Log.v("USERSEX", userSex);
        Log.v("OctaveHighLow", octaveHighLow);

        File sdCard = Environment.getExternalStorageDirectory();
        file = new File(sdCard, filename);

        getOctaveMusicData();
        fetchAudioUrlFromFirebase();

        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        pitchTextView = findViewById(R.id.pitchTextView);
        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    recordAudio();
                    isRecording = true;
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                    isRecording = false;
                }
                Intent intent = new Intent(getApplicationContext(), OctaveTestEndActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("userSex", userSex);
                intent.putExtra("octaveHighLow", octaveHighLow);
                startActivity(intent);
                finish();
            }
        });
    }

    String prevOctave;

    public void recordAudio() {

        releaseDispatcher();
        createMediaPlayer();

        prevOctave = "";
        map = new HashMap<>(); // 녹음될 때마다 map 초기화

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
                            double time = (end - musicStartTime) / (1000000000.0);

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

    private void createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(musicUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    musicStartTime = System.nanoTime(); // 시작 시간 측정
                }
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("MEDIAPLAYER", e.getMessage());
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
        stopMediaPlayer();
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void releaseDispatcher() {
        if (dispatcher != null) {
            if (!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseDispatcher();
    }

    public void addDataToFireStore(Object[] mapkey) {

        ArrayList<UserMusicDto> userMusicInfoList = checkUserDataIsInRange(mapkey);
        calcUserScore(userMusicInfoList);
    }

    public void addWAVToFireStorage() {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        StorageReference filepath = mStorage.child("User").child(userEmail).child("octaveTest").child(octaveHighLow);

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

        boolean flag = false;
        for (Object key : mapkey) {
            try {
                startTime = startTimeList.get(idx);
                nextStartTime = endTimeList.get(idx);
            } catch (IndexOutOfBoundsException e) {
                // 다음 소절이 존재하지 않는 경우
                nextStartTime = 50.0;
            }
            double dKey = Double.parseDouble(key.toString());
            // 소절이 시작한 뒤 입력된 음성만 처리
            if (startTimeList.get(0) <= dKey && endTimeList.get(endTimeList.size() - 1) >= dKey) {
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

        if (!flag) {
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

    public void calcUserScore(ArrayList<UserMusicDto> userMusicInfoList) {

        int userTotalCorrectNoteNum = 0;
        int userTotalCorrectRhythmNum = 0;
        int userTotalTestNoteNum = 0;
        int userOctaveNoteNum;

        int bestOctaveIdx = 0;
        double bestOctaveScore = 0;

        int sentenceIdx = 0;
        DecimalFormat df = new DecimalFormat("0.00");

        for (UserMusicDto userMusicDto : userMusicInfoList) {
            int noteIdx = 0;
            int userCorrectNoteNum = 0;
            int userCorrectRhythmNum = 0;

            TestMusicDto testMusicDto = musicTotalInfoList.get(sentenceIdx);
            ArrayList<NoteDto> testNoteDtoList = testMusicDto.getNotes();
            Double octaveStartTime = testMusicDto.getStartTime();
            Double octaveEndTime = testMusicDto.getEndTime();
            Double testNoteEndTime = Double.parseDouble(testNoteDtoList.get(noteIdx).getEndTime());

            boolean isWrongNote = false;
            boolean isFirstNote = false;

            ArrayList<UserNoteDto> userNoteDtoList = userMusicDto.getNotes();
            Double userNoteStartTime = 0.0;

            for (UserNoteDto userNoteDto : userNoteDtoList) {
                userNoteStartTime = Double.parseDouble(userNoteDto.getStartTime());
                // 1. 옥타브 범위 내에 있을 때
                if (octaveStartTime <= userNoteStartTime && octaveEndTime >= userNoteStartTime) {
                    // 2. note의 허용 시간 계산
                    ArrayList<Double> timeRangeList = processTimeRange(userNoteDto.getStartTime());
                    if (testNoteEndTime <= userNoteStartTime) {
                        noteIdx++;
                        testNoteEndTime = Double.parseDouble(testNoteDtoList.get(noteIdx).getEndTime());
                        timeRangeList = processTimeRange(testNoteDtoList.get(noteIdx).getStartTime());
                        isFirstNote = false;
                    }
                    if (!isFirstNote && calcStartTimeRange(timeRangeList, userNoteDto.getStartTime())) {
                        userCorrectRhythmNum++;
                        isFirstNote = true;
                    }
                    ArrayList<String> noteRangeList = processNoteRange(testNoteDtoList.get(noteIdx).getNote());
                    if (!noteRangeList.contains(userNoteDto.getNote()))
                        isWrongNote = true;
                }

                if (!isWrongNote)
                    userCorrectNoteNum++;
            }
            sentenceIdx++;

            userTotalCorrectNoteNum += userCorrectNoteNum;
            userTotalCorrectRhythmNum += userCorrectRhythmNum;
            userOctaveNoteNum = userNoteDtoList.size();
            userTotalTestNoteNum += userOctaveNoteNum;

            Double octaveNoteScore = ((double) userCorrectNoteNum / userOctaveNoteNum) * 100;
            Double octaveRhythmScore = ((double) userCorrectRhythmNum / userOctaveNoteNum) * 100;
            Double octaveTotalScore = (octaveNoteScore + octaveRhythmScore) / 2;

            userMusicDto.setNoteScore(df.format(octaveNoteScore));
            userMusicDto.setRhythmScore(df.format(octaveRhythmScore));
            userMusicDto.setTotalScore(df.format(octaveTotalScore));

            if (bestOctaveScore <= octaveTotalScore) {
                bestOctaveScore = octaveTotalScore;
                bestOctaveIdx = sentenceIdx - 1;
            }
        }

        double totalNoteScore = ((double) userTotalCorrectNoteNum / userTotalTestNoteNum) * 100;
        double totalRhythmScore = ((double) userTotalCorrectRhythmNum / userTotalTestNoteNum) * 100;
        double totalScore = (totalNoteScore + totalRhythmScore) / 2;

        UserSongInfoDto userSongInfoDto = UserSongInfoDto
                .builder()
                .songInfo(userMusicInfoList)
                .totalScore(df.format(totalScore))
                .noteScore(df.format(totalNoteScore))
                .rhythmScore(df.format(totalRhythmScore))
                .build();

        uploadUserScore(userSongInfoDto);
        uploadBestOctaveIdx(bestOctaveIdx);

    }

    private void uploadUserScore(UserSongInfoDto userSongInfoDto) {

        Map<String, UserSongInfoDto> userMusicList = new HashMap<>();
        userMusicList.put("octaves", userSongInfoDto);

        database.collection("User").document(userEmail).collection("userOctaveList").document(octaveHighLow)
                .set(userMusicList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.v("음정 점수 산출", "success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("음정 점수 산출", "failed");
                    }
                });
    }

    public void uploadBestOctaveIdx(int bestOctaveIdx) {

        String userOctave;
        if (octaveHighLow.equals("high"))
            userOctave = "maxUserPitch";
        else
            userOctave = "minUserPitch";

        database.collection("User").document(userEmail)
                .update(userOctave, bestOctaveIdx)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.v("음정 점수 산출", "success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("음정 점수 산출", "failed");
                    }
                });
    }

    private void getOctaveMusicData() {
        Task<DocumentSnapshot> querySnapshot = database.collection("OctaveTest").document(userSex)
                .collection("highLowTest").document(octaveHighLow).get();
        querySnapshot
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        HashMap<String, Object> testMap = (HashMap<String, Object>) Objects.requireNonNull(documentSnapshot.getData()).get("tests");
                        ArrayList<HashMap<String, Object>> octaveVersion = (ArrayList<HashMap<String, Object>>) testMap.get("octaves");
                        double noteTime = (double) testMap.get("note_time");

                        startTimeList = new ArrayList<>();
                        endTimeList = new ArrayList<>();
                        musicTotalInfoList = new ArrayList<>();

                        for (int i = 0; i < octaveVersion.size(); i++) {
                            HashMap<String, Object> noteListMap = octaveVersion.get(i);
                            double octaveStartTime = Double.parseDouble(String.valueOf(noteListMap.get("start_time")));
                            ArrayList<String> noteList = (ArrayList<String>) noteListMap.get("notes");
                            double octaveEndTime = octaveStartTime + noteTime * noteList.size();

                            ArrayList<NoteDto> noteDtoArrayList = new ArrayList<>();
                            for (int j = 0; j < noteList.size(); j++) {
                                NoteDto noteDto = NoteDto.builder()
                                        .startTime(String.valueOf(octaveStartTime + noteTime * j))
                                        .note(noteList.get(j))
                                        .endTime(String.valueOf(octaveStartTime + noteTime * (j + 1)))
                                        .build();
                                noteDtoArrayList.add(noteDto);
                            }

                            TestMusicDto testMusicDto = TestMusicDto.builder()
                                    .startTime(octaveStartTime)
                                    .endTime(octaveEndTime)
                                    .notes(noteDtoArrayList)
                                    .build();

                            startTimeList.add(octaveStartTime);
                            endTimeList.add(octaveEndTime);
                            musicTotalInfoList.add(testMusicDto);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("ERROR", "not enough records for calculating");
                    }
                });
    }

    // stream music directly from firebase
    private void fetchAudioUrlFromFirebase() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        String url = "gs://certune-73ce6.appspot.com/" + userSex + "Pitch/" + userSex + octaveHighLow + ".mp3";
        StorageReference storageRef = storage.getReferenceFromUrl(url);

        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        musicUrl = uri.toString();
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
