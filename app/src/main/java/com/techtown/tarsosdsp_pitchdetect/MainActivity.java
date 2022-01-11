package com.techtown.tarsosdsp_pitchdetect;

import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final String TAG = "Main_Activity";
    private FirebaseFirestore db;
    private List<SongList> songLists = new ArrayList<>();



    // 실시간 데이터 변경 감지
    private void myStream() {
        final DocumentReference docRef = db.collection("songList").document("신호등");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "myStream : onEvent: 데이터 변경됨");
                SongList songList = documentSnapshot.toObject(SongList.class);
                Log.d(TAG, "myStream : onEvent: user : " + songList);
            }
        });
    }







    Map<Double, String> map; // {key : octav}
    Map<Double, String> musicMap;

    AudioDispatcher dispatcher;
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    MediaPlayer mediaPlayer;

    File file;
    File audiofile;

    TextView pitchTextView;
    Button recordButton;
    Button playButton;

    boolean isRecording = false;
    String filename = "recorded_sound.wav";



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 양방향 stream
        db = FirebaseFirestore.getInstance();
        myStream();

        File sdCard = Environment.getExternalStorageDirectory();
        audiofile = new File(sdCard, filename);
        // 실패 ) 핸드폰의 동일한 위치에 mp3/wav 파일을 넣어놓고 재생시키는 방법 - 여전히 지지직 소리
        //audiofile = new File(sdCard, audiofilename);

        /*
        filePath = file.getAbsolutePath();
        Log.e("MainActivity", "저장 파일 경로 :" + filePath); // 저장 파일 경로 : /storage/emulated/0/recorded.mp4
        */

        tarsosDSPAudioFormat=new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
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
                if(!isRecording)
                {
                    recordAudio();
                    isRecording = true;
                    recordButton.setText("중지");
                }
                else
                {
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

    public void playAudio()
    {
        musicMap = new HashMap<>(); // 녹음될 때마다 사용자 음성 담은 map 초기화
        long start = System.currentTimeMillis(); // 시작 시간 측정
        try{
            releaseDispatcher();

            // 성공 ) 얘는 dispatcher와 별개로 돌아가는 메소드
            //mediaPlayer = MediaPlayer.create(this, R.raw.music);
            //mediaPlayer.start();

            // 실패 ) res/raw 밑에 있는 파일 읽어오기 -> 지지직 소리 해결 X
            //InputStream fileInputStream = getResources().openRawResource(R.raw.music_wav);
            FileInputStream fileInputStream = new FileInputStream(audiofile);
            dispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);

            AudioProcessor playerProcessor = new AndroidAudioPlayer(tarsosDSPAudioFormat, 2048, 0);
            dispatcher.addAudioProcessor(playerProcessor);

            PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e){
                    final float pitchInHz = res.getPitch();
                    String octav = ProcessPitch.processPitch(pitchInHz);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pitchTextView.setText(octav);
                            long end = System.currentTimeMillis();
                            double time = (end-start)/(1000.0);

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

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    public void recordAudio()
    {
        map = new HashMap<>(); // 녹음될 때마다 map 초기화
        long start = System.currentTimeMillis(); // 시작 시간 측정

        Log.v("start","start time measuring process");
        releaseDispatcher();
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        try {
            Log.v("start2", "try문");
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            AudioProcessor recordProcessor = new WriterProcessor(tarsosDSPAudioFormat, randomAccessFile);
            dispatcher.addAudioProcessor(recordProcessor);

            PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e){
                    final float pitchInHz = res.getPitch();
                    String octav = ProcessPitch.processPitch(pitchInHz);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pitchTextView.setText(octav);
                            long end = System.currentTimeMillis();
                            double time = (end-start)/(1000.0);

                            if (!octav.equals("Nope")) {// 의미있는 값일 때만 입력받음
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

    public void stopRecording()
    {
        // map에서 꺼내는 코드
        Log.v("end", "break");

        // 키로 정렬
        Object[] mapkey = map.keySet().toArray();
        Arrays.sort(mapkey);
        for (Object key : mapkey){
            Log.v("please", String.valueOf(key) + "/ value: "+map.get(key));
        }
        Log.v("end", "break2");
        releaseDispatcher();
    }

    public void releaseDispatcher()
    {
        if(dispatcher != null)
        {
            if(!dispatcher.isStopped())
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