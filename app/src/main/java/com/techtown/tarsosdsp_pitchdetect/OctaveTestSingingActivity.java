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
import com.techtown.tarsosdsp_pitchdetect.domain.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.SongSentenceDto;
import com.techtown.tarsosdsp_pitchdetect.domain.SongTotalNoteTimeDto;
import com.techtown.tarsosdsp_pitchdetect.domain.TestMusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserMusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserNoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserSongInfoDto;
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

public class OctaveTestSingingActivity extends AppCompatActivity {

    private String userEmail;
    private String userSex;
    private String octaveHighLow;

    TextView pitchTextView;
    Button recordButton;
    Button playButton;

    Map<Double, String> map;
    Map<Double, String> musicMap;

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

        File sdCard = Environment.getExternalStorageDirectory();
        file = new File(sdCard, filename);

        /*
        filePath = file.getAbsolutePath();
        Log.e("MainActivity", "저장 파일 경로 :" + filePath); // 저장 파일 경로 : /storage/emulated/0/recorded.mp4
        */

        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

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

    public void playAudio() {
        musicMap = new HashMap<>(); // 녹음될 때마다 사용자 음성 담은 map 초기화
        long start = System.nanoTime(); // 시작 시간 측정
        try {
            releaseDispatcher();

            FileInputStream fileInputStream = new FileInputStream(file);
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

        prevOctave = "";
        map = new HashMap<>(); // 녹음될 때마다 map 초기화
        long start = System.nanoTime(); // 시작 시간 측정

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseDispatcher();
    }

    public void addDataToFireStore(Object[] mapkey) {

        ArrayList<UserMusicDto> userMusicInfoList = checkUserDataIsInRange(mapkey);
        // TODO : songName 반영
        calcUserScore(userMusicInfoList);
    }

    public void addWAVToFireStorage() {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        // User/{userName}/octaveTest/{octaveHighMidLow}
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
            // 소절이 시작한 뒤 입력된 음성만 처리
            if (startTimeList.get(0) <= Double.parseDouble(key.toString())) {
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

        int userTotalNoteNum = 0;
        double userTotalSentenceTime = 0;

        double bestOctaveScore = 0;
        int bestOctaveIdx = 0;

        SongTotalNoteTimeDto songTotalNoteTimeDto = new SongTotalNoteTimeDto();
        List<SongSentenceDto> songSentenceInfoList = getSongSentenceInfo(songTotalNoteTimeDto);

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
            sentenceIdx++;

            userTotalSentenceTime += userSentenceTime;
            userTotalNoteNum += userCorrectNoteNum;

            Double sentenceDurationTime = songSentenceDto.getSentenceDurationTime();
            int songSentenceNoteNum = songSentenceDto.getSentenceNoteNum();

            Double sentenceNoteScore = (userSentenceTime / sentenceDurationTime) * 100;
            Double sentenceRhythmScore = ((double) userCorrectNoteNum / songSentenceNoteNum) * 100;
            Double sentenceTotalScore = (sentenceNoteScore + sentenceRhythmScore) / 2;

            userMusicDto.setNoteScore(df.format(sentenceNoteScore));
            userMusicDto.setRhythmScore(df.format(sentenceRhythmScore));
            userMusicDto.setTotalScore(df.format(sentenceTotalScore));

            if (bestOctaveScore < sentenceTotalScore) {
                bestOctaveScore = sentenceTotalScore;
                bestOctaveIdx = sentenceIdx - 1;
            }
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

        uploadUserScore(userSongInfoDto);
        uploadBestOctaveIdx(bestOctaveIdx);

    }

    private List<SongSentenceDto> getSongSentenceInfo(SongTotalNoteTimeDto songTotalNoteTimeDto) {
        List<SongSentenceDto> songSentenceInfoList = new ArrayList<>();
        for (TestMusicDto musicinfo : musicTotalInfoList) {

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
                .collection("highLowTest").document("high").get();
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

                            ArrayList<NoteDto> noteDtoArrayList = new ArrayList<>();
                            for (int j = 0; j < noteList.size(); j++) {
                                NoteDto noteDto = NoteDto.builder()
                                        .startTime(String.valueOf(octaveStartTime + noteTime * i))
                                        .note(noteList.get(i))
                                        .endTime(String.valueOf(octaveStartTime + noteTime * (i + 1)))
                                        .build();
                                noteDtoArrayList.add(noteDto);
                            }

                            TestMusicDto testMusicDto = TestMusicDto.builder()
                                    .startTime(String.valueOf(octaveStartTime))
                                    .endTime(String.valueOf(octaveStartTime + noteTime * 5))
                                    .notes(noteDtoArrayList)
                                    .build();

                            Log.v("옥타브 시작 시간", String.valueOf(octaveStartTime));
                            Log.v("옥타브 노트 시간", String.valueOf(noteTime));
                            Log.v("옥타브 종료 시간", String.valueOf(octaveStartTime + noteTime * 5));
                            startTimeList.add(octaveStartTime);
                            endTimeList.add(octaveStartTime + noteTime * 5);
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
                        try {
                            final String downloadUrl = uri.toString();
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(downloadUrl);
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });
                            mediaPlayer.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

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
