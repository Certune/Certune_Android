package com.techtown.tarsosdsp_pitchdetect.OctaveTest.activity;

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
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.Singing.activity.LoadingDialog;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.OctaveTest.domain.TestMusicInfoDto;
import com.techtown.tarsosdsp_pitchdetect.score.domain.UserMusicDto;
import com.techtown.tarsosdsp_pitchdetect.score.domain.UserNoteDto;
import com.techtown.tarsosdsp_pitchdetect.OctaveTest.domain.TestUserResultDto;
import com.techtown.tarsosdsp_pitchdetect.score.logics.ProcessPitch;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

public class TestSingingActivity extends AppCompatActivity {

    final int highestNoteIdx = 4;
    final int lowestNoteIdx = 0;

    private String userEmail;
    private String userSex;
    private String octaveHighLow;

    LoadingDialog dialog;

    HorizontalScrollView scrollView;

    Map<Double, String> map;
    String musicUrl;

    ArrayList<Double> startTimeList;
    ArrayList<Double> endTimeList;
    ArrayList<TestMusicInfoDto> musicTotalInfoList;

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

        scrollView = findViewById(R.id.octaveTest_scrollView);

        dialog = new LoadingDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    String prevOctave;

    public void recordAudio() {
        releaseDispatcher();
        mediaPlayer.start();
        setScrollSettings();
        long start = System.nanoTime();
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
        StorageReference filepath = mStorage.child("User").child(userEmail).child("octaveTest").child(octaveHighLow+"result");

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

            TestMusicInfoDto testMusicDto = musicTotalInfoList.get(sentenceIdx);
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

        TestUserResultDto testUserResultDto = TestUserResultDto
                .builder()
                .songInfo(userMusicInfoList)
                .totalScore(df.format(totalScore))
                .noteScore(df.format(totalNoteScore))
                .rhythmScore(df.format(totalRhythmScore))
                .build();

        uploadUserScore(testUserResultDto);
        findBestOctaveNote(bestOctaveIdx);
    }

    private void uploadUserScore(TestUserResultDto testUserResultDto) {

        Map<String, TestUserResultDto> userMusicList = new HashMap<>();
        userMusicList.put("octaves", testUserResultDto);

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

    private void findBestOctaveNote(int bestOctaveIdx) {
        Log.v("BEST OCTAVE IDX", String.valueOf(bestOctaveIdx));
        database.collection("OctaveTest").document(userSex)
                .collection("highLowTest").document(octaveHighLow)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                try {
                    ArrayList<HashMap<String, Object>> octaves = (ArrayList<HashMap<String, Object>>) document.get("octaves");
                    HashMap<String, Object> notemap = octaves.get(bestOctaveIdx);
                    ArrayList<String> notes = (ArrayList<String>) notemap.get("notes");
                    String bestOctaveNote;
                    if (octaveHighLow.equals("high"))
                        bestOctaveNote = notes.get(highestNoteIdx);
                    else
                        bestOctaveNote = notes.get(lowestNoteIdx);
                    Log.v("베스트노트", bestOctaveNote);
                    uploadBestOctaveIdx(bestOctaveNote);
                } catch (Exception e) {
                    Log.e("Best Octave Note", "해당 노트를 찾을 수 없습니다.");
                }
            }
            else {
                Log.e("Best Octave Note", "OctaveTest document를 찾을 수 없습니다.");
            }
        });
    }

    public void uploadBestOctaveIdx(String bestOctaveNote) {

        String userOctave;
        if (octaveHighLow.equals("high"))
            userOctave = "maxUserPitch";
        else
            userOctave = "minUserPitch";

        database.collection("User").document(userEmail)
                .update(userOctave, bestOctaveNote)
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
                        double noteTime = (double) documentSnapshot.get("note_time");
                        ArrayList<HashMap<String, Object>> octaves = (ArrayList<HashMap<String, Object>>) documentSnapshot.get("octaves");

                        startTimeList = new ArrayList<>();
                        endTimeList = new ArrayList<>();
                        musicTotalInfoList = new ArrayList<>();

                        for (int i= 0; i < octaves.size(); i++) {
                            HashMap<String, Object> noteListMap = octaves.get(i);
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

                            TestMusicInfoDto testMusicDto = TestMusicInfoDto.builder()
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

    /* Mediaplayer 설정 */
    public void createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(musicUrl);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    dialog.dismiss();
                    if (!isRecording) {
                        recordAudio();
                        isRecording = true;
                    }
                }
            });
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (isRecording) {
                        stopRecording();
                        isRecording = false;
                    }
                    Intent intent = new Intent(getApplicationContext(), TestEndActivity.class);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("userSex", userSex);
                    intent.putExtra("octaveHighLow", octaveHighLow);
                    startActivity(intent);
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e("MEDIAPLAYER", e.getMessage());
        }
    }

    private void fetchAudioUrlFromFirebase() {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference storageRef = storage.child(userSex+"Pitch").child(userSex+octaveHighLow+ ".mp3");

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

    private void setScrollSettings() {
        LinearInterpolator interpolator = new LinearInterpolator();
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollX", 1000000);
                objectAnimator.setDuration(Math.round(27 * 100000)); // TODO : SONG 길이에 맞춰서 넣어줘야 함
                objectAnimator.setInterpolator(new LinearInterpolator());
                objectAnimator.start();
            }
        });
    }


}
