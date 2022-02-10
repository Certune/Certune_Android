package com.techtown.tarsosdsp_pitchdetect;

import static com.techtown.tarsosdsp_pitchdetect.ProcessNoteRange.processNoteRange;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techtown.tarsosdsp_pitchdetect.domain.MusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserMusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserNoteDto;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
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


public class MainActivity extends AppCompatActivity {
    Map<Double, String> map; // {key : octav}
    Map<Double, String> musicMap;

    // 곡의 소절별 시작 시간을 담은 ArrayList
    ArrayList<Double> startTimeList = new ArrayList<>();
    // 곡의 모든 정보를 담은 ArrayList
    ArrayList<MusicDto> musicInfoList = new ArrayList<>();

    Integer startTimeIndex = 0;

    // 곡의 소절별 시작 시간을 담은 ArrayList
    ArrayList<Double> endTimeList = new ArrayList<>();
    Integer endTimeIndex = 0;

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
        database.document("song1/sentence").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        List list = (List) Objects.requireNonNull(document.getData()).get("sentences");
                        for (int i = 0; i < Objects.requireNonNull(list).size(); i++) {
                            HashMap<String, ArrayList<HashMap<String, Object>>> map = (HashMap) list.get(i);
                            ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");
                            ArrayList<NoteDto> noteDtoArrayList = new ArrayList<>();
                            assert arrayMap != null;
                            for (HashMap<String, Object> notemap : arrayMap){
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
                            startTimeList.add(Double.parseDouble(musicDto.getStart_time()));
                            endTimeList.add(Double.parseDouble(musicDto.getEnd_time()));
                            // TODO : MusicDto 전체 받아오는 LIST 만들기(점수 산출용)
                            musicInfoList.add(musicDto);

                            NoteDto noteDtoTest = musicDto.getNotes().get(0);
                        }
                    } else {
                        // 실패
                    }
                }
        );

        // mediaplayer setting
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        fetchAudioUrlFromFirebase();

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
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/certune-73ce6.appspot.com/o/%EC%8B%A0%ED%98%B8%EB%93%B1_%EC%9D%B4%EB%AC%B4%EC%A7%84.mp3?alt=media&token=4bbb1db6-22ff-4823-b4fc-af14696504bd");
        storageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            // Download url of file
                            final String url = uri.toString();
                            mediaPlayer.setDataSource(url);
                            // wait for media player to get prepare
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
                        Log.i("TAG", e.getMessage());
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

            //RandomAccessFile randomAccessAudioFile = new RandomAccessFile(newAudioFile, "rw");
            //AudioProcessor recordProcessorAudio = new WriterProcessor(tarsosDSPAudioFormat, randomAccessAudioFile);
            //dispatcher.addAudioProcessor(recordProcessorAudio);

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
        // startTimeIndex 초기화
        startTimeIndex = 0;

        // 사용자가 부른 정보를 키로 정렬
        Object[] mapkey = map.keySet().toArray();
        Arrays.sort(mapkey);
        for (Object key : mapkey) {
            Log.v("result", String.valueOf(key) + "/ value: " + map.get(key));
        }
        // TODO : DB로 wav file 보내기
        addWAVToFireStorage();

        // DB로 값 보내기
        addDataToFireStore(mapkey);

        // TODO: DB에서 값 받아서 비교 -> 점수산출

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
        // TODO : 사용자 회원가입 시 COLLECTION 생성 / 해당 COLLECTION에 모든 정보 저장
        CollectionReference userNote = database.collection("user1"); // 이건 회원가입 때 만들어야 함

        ArrayList<UserNoteDto> noteList = new ArrayList<>();

        int idx = 0;
        Double startTime = 0.0;
        Double nextStartTime = 0.0;

        ArrayList<UserMusicDto> sentenceList = new ArrayList<>();
        Map<String, ArrayList<UserMusicDto>> userMusicList = new HashMap<>();

        boolean flag = false;
        for (Object key : mapkey) {
            try  {
                startTime = startTimeList.get(idx);
                nextStartTime = endTimeList.get(idx);
            } catch (IndexOutOfBoundsException e) {
                // 다음 소절이 존재하지 않는 경우
                //
                nextStartTime = 50.0;
            };


            // 소절이 시작한 뒤 입력된 음성만 처리
            if(startTimeList.get(0) <= Double.parseDouble(key.toString())) {
                if (nextStartTime > Double.parseDouble(key.toString())) {
                    flag = false;
                    // 다음 소절 전까지 noteList에 note 담음
                    noteList.add(new UserNoteDto(String.valueOf(key), map.get(key)));

                } else { // 다음 소절로 넘어갔을 때 이전 소절에 대한 처리
                    flag = true;
                    UserMusicDto userMusicDto = new UserMusicDto(String.valueOf(startTime), noteList, "null");
                    sentenceList.add(userMusicDto);

                    userMusicList.put("sentence", sentenceList);
                    idx++;

                    // 한 소절에 대한 처리가 끝난 후 noteList 초기화 및 직전에 들어온 값 add
                    noteList = new ArrayList<>();
                    noteList.add(new UserNoteDto(String.valueOf(key), map.get(key)));
                }

            }

        }

        if(flag == false) {
            UserMusicDto userMusicDto = new UserMusicDto(String.valueOf(startTime), noteList, "null");
            sentenceList.add(userMusicDto);

            userMusicList.put("sentence", sentenceList);
        }

        database.document("user1/song0")
                .set(userMusicList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.v("TAG", "success");
                        getUserMusicInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("TAG", "failed");
                    }
                });
    }

    public void addWAVToFireStorage(){
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

    public void getUserMusicInfo(){
        ArrayList<UserMusicDto> userMusicInfoList = new ArrayList<>();
        // TODO : song 이름 변수로 넣어줘야 함
        database.document("user1/song0").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        try{
                            List list = (List) Objects.requireNonNull(document.getData()).get("sentence");
                            for (int i = 0; i < Objects.requireNonNull(list).size(); i++) {
                                // start_time, score, note arraylist 있는 해시맵
                                HashMap<String, ArrayList<HashMap<String, Object>>> map = (HashMap) list.get(i);
                                // note가 있는 배열
                                ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");
                                assert arrayMap != null;
                                ArrayList<UserNoteDto> userMusicDtoArrayList = new ArrayList<>();
                                for (HashMap<String, Object> notemap : arrayMap){
                                    UserNoteDto userNoteDto = new UserNoteDto(
                                            String.valueOf(notemap.get("start_time")),
                                            String.valueOf(notemap.get("note"))
                                    );
                                    userMusicDtoArrayList.add(userNoteDto);
                                }
                                UserMusicDto musicDto = new UserMusicDto(
                                        String.valueOf(map.get("start_time")),
                                        userMusicDtoArrayList,
                                        null
                                );
                                userMusicInfoList.add(musicDto);
                                Log.i("GET FROM DB", musicDto.getStart_time()+musicDto.getNotes());
                            }
                            calcScore(userMusicInfoList);
                        } catch (NullPointerException e) {
                            Log.v("ERROR", "not enough records for calculating");
                        }
                    }
                }
        );
    }

    public void calcScore(ArrayList<UserMusicDto> userMusicInfoList){
        // 나중에 지욱
        ArrayList<String> scoreList = new ArrayList<>();

        Map<String, ArrayList<UserMusicDto>> userMusicList = new HashMap<>();
        Double sentenceTime = null; // 한 소절의 지속 시간
        Double userSentenceTime = null; // 유저가 부른 소절의 일치 시간
        int sentenceIdx = 0;
        int noteIdx = 0;

        ArrayList<Double> startTimeList = new ArrayList<>();
        ArrayList<Double> endTimeList = new ArrayList<>();
        ArrayList<String> noteList = new ArrayList<>();

        // songDB에서 소절별 NOTE 정보 받아오기
        for (MusicDto musicinfo : musicInfoList) { // [songDB -> 소절]
            sentenceTime = Double.parseDouble(musicinfo.getEnd_time()) - Double.parseDouble(musicinfo.getStart_time());
            Log.v("SENTENCE ALL TIME", String.valueOf(sentenceTime));
            userSentenceTime = sentenceTime;
            ArrayList<NoteDto> musicNoteDtos = musicinfo.getNotes(); // 소절의 note 정보

            // songDB에 있는 noteDto의 정보를 담기
            for (NoteDto musicNote : musicNoteDtos) { // [songDB -> NOTE 정보]
                Log.i("songDB note info", musicNote.getStart_time() + "/" + musicNote.getEnd_time() + "/" + musicNote.getNote());
                startTimeList.add(Double.parseDouble(musicNote.getStart_time()));
                endTimeList.add(Double.parseDouble(musicNote.getEnd_time()));
                noteList.add(musicNote.getNote());
            }
        }

        for (UserMusicDto userMusicDto : userMusicInfoList){ // [userDB -> 소절]
            Log.i("WHY", "THIS IS NOT WORKING");
            ArrayList<UserNoteDto> userNotes = userMusicDto.getNotes();

            Double sentenceStartTime = startTimeList.get(sentenceIdx);
            Double sentenceEndTime = endTimeList.get(sentenceIdx);

            Double noteStartTime = sentenceStartTime;
            Boolean flag = false;

            for (UserNoteDto userNoteDto : userMusicDto.getNotes()) {
                if (flag){
                    userSentenceTime -= Double.parseDouble(userNoteDto.getStart_time())-noteStartTime;
                    flag = false;
                }
                // 소절이 시작한 뒤 입력된 음성만 처리
                ArrayList<String> noteLenList = processNoteRange(userNoteDto.getNote());
                if ((startTimeList.get(sentenceIdx) <= noteStartTime && endTimeList.get(sentenceIdx) >= noteStartTime)
                        & noteLenList.contains(userNoteDto.getNote())) { // 시작 시간 내에 있고 일정 범위에 포함되는 경우
                    continue;
                }
                flag = true;
                noteStartTime = Double.parseDouble(userNoteDto.getStart_time());
            }
            if (flag){ // 소절이 끝났을 떄 처리
                userSentenceTime -= sentenceEndTime-noteStartTime;
            }
            sentenceIdx++;

            Double score = (userSentenceTime/sentenceTime)*100;
            Log.v("SCORE CHECK", String.valueOf(score));
            userMusicDto.setScore(String.valueOf(score));
        }


//         소절 점수 db에 올리기(수정 필요)
//        database.document("user1/song123")
//                .set(userMusicInfoList)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Log.v("TAG", "success");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.v("TAG", "failed");
//                    }
//                });
    }
}
