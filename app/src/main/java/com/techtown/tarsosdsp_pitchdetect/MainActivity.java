package com.techtown.tarsosdsp_pitchdetect;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techtown.tarsosdsp_pitchdetect.domain.MusicDto;
import com.techtown.tarsosdsp_pitchdetect.domain.NoteDto;
import com.techtown.tarsosdsp_pitchdetect.domain.UserMusicDto;


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
    Integer startTimeIndex = 0;

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
                        List list = (List) document.getData().get("sentences");
                        for (int i = 0; i < list.size(); i++) {
                            HashMap map = (HashMap) list.get(i);
                            MusicDto musicDto = new MusicDto(
                                    Objects.requireNonNull(map.get("start_time")).toString(),
                                    Objects.requireNonNull(map.get("end_time")).toString(),
                                    Objects.requireNonNull(map.get("lyrics")).toString(),
                                    (ArrayList<NoteDto>) map.get("notes")
                            );

                            // ArrayList에 소절별 시작 시간을 담기
                            startTimeList.add(Double.parseDouble(musicDto.getStart_time()));

                            Log.i("TEST", musicDto.getLyrics() + "/" + musicDto.getStart_time() + "/" + musicDto.getEnd_time()
                                    + "//" + musicDto.getStart_time() + "/" + musicDto.getEnd_time() + "/" + musicDto.getNotes());

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


    public void recordAudio() {
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

                                map.put(time, octav);
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
        // TODO : DB로 값 보내기
        addDataToFireStore(mapkey);

        // TODO: DB에서 값 받아서 비교

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

        ArrayList<String> noteList = new ArrayList<>();

        int idx = 0;
        Double startTime = 0.0;
        Double nextStartTime = 0.0;

        Map<String, UserMusicDto> userMusicList = new HashMap<>();
        for (Object key : mapkey) {
            try  {
                startTime = startTimeList.get(idx);
                nextStartTime = startTimeList.get(idx + 1);
            } catch (IndexOutOfBoundsException e) {
                // 다음 소절이 존재하지 않는 경우
                nextStartTime = 1000000000.0;
            };

            // 소절이 시작한 뒤 입력된 음성만 처리
            if(startTimeList.get(0) <= Double.parseDouble(key.toString())) {
                if (nextStartTime > Double.parseDouble(key.toString())) {
                    // 다음 소절 전까지 noteList에 note 담음
                    noteList.add(map.get(key));

                } else { // 다음 소절로 넘어갔을 때 이전 소절에 대한 처리
                    UserMusicDto userMusicDto = new UserMusicDto(String.valueOf(startTime), noteList, "null");

                    // userMusicList에 idx, userMusicDto 넣기
                    if (idx >= 0 && idx <= 9) { // additional sorting
                        userMusicList.put("0" + idx, userMusicDto);
                    } else {
                        userMusicList.put(String.valueOf(idx), userMusicDto);
                    }
                    idx++;

                    // 한 소절에 대한 처리가 끝난 후 noteList 초기화 및 직전에 들어온 값 add
                    noteList = new ArrayList<>();
                    noteList.add(map.get(key));
                }
            }


        }

        database.document("user1/song0")
                .set(userMusicList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.v("TAG", "success");
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
}
