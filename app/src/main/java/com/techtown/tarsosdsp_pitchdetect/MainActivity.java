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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
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
    Map<Double, String> map; // {key : octave}
    Map<Double, String> musicMap;

    AudioDispatcher dispatcher;
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    MediaPlayer mediaPlayer;

    File file;

    TextView pitchTextView;
    Button recordButton;
    Button playButton;

    boolean isRecording = false;

    String fileName = "recorded_sound.wav";
    String audioFileName = "music_wav.wav";
    String newAudioFileName = "new.wav";

    // firebase db 연동
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        fetchAudioUrlFromFirebase();

        // Get a top-level shared/external storage directory for placing files of a particular type.
        File sdCard = Environment.getExternalStorageDirectory();
        file = new File(sdCard, fileName); // File(File parent, String Child)

        // PCM(Pulse Code Modulation): 아날로그 신호 -> 양자화, 부호화 단계를 거쳐 2진 부호 형태로 전송하는 변조 방식
        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        // Read from database
        database.document("song1/note1").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //성공
                        DocumentSnapshot document = task.getResult();
                        List list = (List) document.getData().get("list");
                        for (int i = 0; i < list.size(); i++) {
                            Log.i("TEST", "data[" + i + "] > " + list.get(i).toString());
                            HashMap map = (HashMap) list.get(i);
                            MusicDto musicDto = new MusicDto(
                                    Objects.requireNonNull(map.get("cumul_time")).toString(),
                                    Objects.requireNonNull(map.get("note")).toString(),
                                    Objects.requireNonNull(map.get("time")).toString()
                            );
                            Log.i("TEST", "[" + i + "] > " + (list.get(i) instanceof HashMap) + " / " + (list.get(i).getClass().getName()) + " / " + list.get(i).toString());
                        }
                    } else {
                        //실패
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

    private void fetchAudioUrlFromFirebase() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/certune-73ce6.appspot.com/o/%EC%8B%A0%ED%98%B8%EB%93%B1_%EC%9D%B4%EB%AC%B4%EC%A7%84.mp3?alt=media&token=4bbb1db6-22ff-4823-b4fc-af14696504bd");
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    // Download url of file
                    final String url = uri.toString();
                    mediaPlayer.setDataSource(url);
                    // wait for media player to get prepare
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("TAG", e.getMessage());
            }
        });
    }

    public void playAudio() {
        musicMap = new HashMap<>(); // 녹음될 때마다 사용자 음성 담은 map 초기화

        long start = System.currentTimeMillis(); // 시작 시간 측정
        try {
            releaseDispatcher();

            // 성공 ) 얘는 dispatcher와 별개로 돌아가는 메소드
            // mediaPlayer = MediaPlayer.create(this, R.raw.music);
            // mediaPlayer.start();

            // 실패 ) res/raw 밑에 있는 파일 읽어오기 -> 지지직 소리 해결 X
            //InputStream fileInputStream = getResources().openRawResource(R.raw.music_wav);
            //FileInputStream fileInputStream = new FileInputStream(audiofile);

            // 실패) asset 폴더 밑의 파일 읽어오기
            //AssetManager assetManager = getAssets();
            //final AssetFileDescriptor fileDescriptor = getResources().openRawResourceFd(R.raw.music_wav);
            //FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());


            FileInputStream fileInputStream = new FileInputStream(file);
            // 마이크가 아닌 파일을 소스로 하는 dispatcher 생성 -> AudioDispatcher 객체 생성 시 UniversalAudioInputStream 사용
            dispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);

//            RandomAccessFile randomAccessAudioFile = new RandomAccessFile(newAudioFile, "rw");
//            AudioProcessor recordProcessorAudio = new WriterProcessor(tarsosDSPAudioFormat, randomAccessAudioFile);
//            dispatcher.addAudioProcessor(recordProcessorAudio);

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
                            long end = System.currentTimeMillis();
                            double time = (end - start) / (1000.0);

                            // 의미있는 값일 때만 입력받음
                            if (!octav.equals("Nope")) {
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
        long start = System.currentTimeMillis(); // 시작 시간 측정

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
                    String octave = ProcessPitch.processPitch(pitchInHz);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pitchTextView.setText(octave);
                            long end = System.currentTimeMillis();
                            double time = (end - start) / (1000.0);

                            // 의미있는 값일 때만 입력받음
                            if (!octave.equals("Nope")) {
                                Log.v("time", String.valueOf(time));
                                map.put(time, octave);
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
        Log.v("end", "break");

        // 키로 정렬해서 pitch 값 가져오기

        Object[] mapkey = map.keySet().toArray();
        Arrays.sort(mapkey);
//        for (Object key : mapkey) {
//            Log.v("result", String.valueOf(key) + "/ value: " + map.get(key));
//        }

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
}