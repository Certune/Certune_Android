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

    /* DB ????????? */
    String songName;
    String sentenceIdx;
    String userEmail;

    String lyric;
    double sentenceStartTime;
    double sentenceEndTime;
    double sentenceNoteEndTime;
    List<NoteDto> sentenceNoteDtoList = new ArrayList<>();

    /* ???????????? */
    // ?????? ??????
    HorizontalScrollView scrollView;
    GridLayout gridLayout;
    Button cell;
    TextView lyricText;
    List<SingingNoteDto> singingNoteDtoList = new ArrayList<>();
    Button pitchGraph;

    // ?????????
    LoadingDialog dialog;
    boolean isNoteLoad = false;
    boolean isMusicLoad = false;

    boolean isSongFirebaseLoad = false;
    boolean isSingingFirebaseLoad = false;

    /* ?????? ?????? */
    String prevOctave;
    Map<Double, String> userMap;
    double calcStartTime;
    double sentenceTotalTime; // ????????? ????????? sentence ?????? ?????? -> db?????? ????????? ??? ??????

    /* ?????? ?????? */
    String musicUrl;
    MediaPlayer mediaPlayer;

    /* TarsosDSP ??? ?????? ?????? */
    TarsosDSPAudioFormat tarsosDSPAudioFormat;
    AudioDispatcher dispatcher;

    File file;
    String filename = "sentenceResult";

    // firestore
    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    /* handler ?????? */
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

        // intent ??????
        Intent subIntent = getIntent();
        songName = subIntent.getStringExtra("songName");
        sentenceIdx = subIntent.getStringExtra("sentenceIdx");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            userEmail = user.getEmail();

        // layout ??????
        scrollView = findViewById(R.id.horizontalScrollView_weakSentence);
        gridLayout = findViewById(R.id.gridLayout_weakSentence);
        lyricText = findViewById(R.id.currentLyricTextView_weakSentence);
        pitchGraph = findViewById(R.id.weak_pitchGraph);

        dialog = new LoadingDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        // url ??????
        fetchAudioUrlFromFirebase();

        // file ??????
        setTarsosDSPSettings();

        // db????????? ???????????? ????????????
        getSentenceInfo();

        // ????????? on
        microphoneOn();

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
                int durationTime = Math.round(11 * 100000 / 20); // TODO : SONG ????????? ????????? ???????????? ???
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
                pitchGraph.setY((float) (1600 - pitchInHz * 3.4));
            });
        };

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }

    public void setCell(int noteIdx, int startTime, int endTime, boolean isNote, boolean isPrepare) {
        Log.v("?????? ?????????", String.valueOf(noteIdx));
        Log.v("?????? ??????", String.valueOf(startTime));
        Log.v("?????? ??????", String.valueOf(endTime));
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
            cell.setBackgroundColor(Color.parseColor("#212121")); // ?????? ????????? ?????? ????????????
        }
        if (isPrepare)
            cell.setBackgroundColor(Color.parseColor("#212121"));

        gridLayout.addView(cell, param);
    }

    public void settingView() {
        gridLayout.setColumnCount((int) Math.round((sentenceEndTime - sentenceStartTime) * 30));
        Log.v("row ??????", String.valueOf(gridLayout.getRowCount()));
        Log.v("column??????", String.valueOf(gridLayout.getColumnCount()));

        // row, col ????????????
        handlerRowSetting.post(runnableRow);
        handlerColSetting.post(runnableCol);
        handlerSong.post(runnableSong);
    }

    /* ?????? ????????? */
    private void startPitchDetection() {
        prevOctave = "";
        userMap = new HashMap<>(); // ???????????? ????????? ????????? ?????? ?????? map ?????????
        calcStartTime = System.nanoTime();
        Log.v("?????? ?????? ??????", String.valueOf(calcStartTime));

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

                            // ???????????? ?????? ?????? ????????????
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
            // ?????? ?????? ?????????
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
            // ?????? key??? ?????? ?????? update
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

        // ?????? ?????? ????????? note???
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

        Log.v("SCORE ??????", userTotalTime + "/" + sentenceTotalTime);
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

    /* DB ?????? */
    private void getSentenceInfo() {
        database.collection("Song").document(songName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                try {
                    ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) document.getData().get("sentence");

                    HashMap<String, Object> sentencemap = (HashMap<String, Object>) list.get(Integer.parseInt(sentenceIdx));

                    lyric = (String) sentencemap.get("lyrics");
                    sentenceEndTime = Double.parseDouble(String.valueOf(sentencemap.get("end_time")));
                    sentenceStartTime = Double.parseDouble(String.valueOf(sentencemap.get("start_time")));
                    Log.v("?????? ??????", lyric);
                    Log.v("?????? ??????", String.valueOf(sentenceStartTime));
                    Log.v("?????? ??????", String.valueOf(sentenceEndTime));

                    lyricText.setText(lyric);

                    ArrayList<HashMap<String, Object>> noteArrayMap = (ArrayList<HashMap<String, Object>>) sentencemap.get("notes");
                    for (HashMap<String, Object> noteMap : noteArrayMap) {
                        NoteDto noteDto = NoteDto.builder()
                                .startTime(String.valueOf(Double.parseDouble((String) noteMap.get("start_time")) - sentenceStartTime))
                                .endTime(String.valueOf(Double.parseDouble((String) noteMap.get("end_time")) - sentenceStartTime))
                                .note(String.valueOf(noteMap.get("note")))
                                .build();

                        Log.v("SONG ??????", noteDto.getStartTime() + " : " + noteDto.getEndTime() + " : " + noteDto.getNote());
                        sentenceNoteDtoList.add(noteDto);
                        sentenceTotalTime += Double.parseDouble(noteDto.getEndTime()) - Double.parseDouble(noteDto.getStartTime());
                        sentenceNoteEndTime = Double.parseDouble(noteDto.getEndTime());
                    }
                    isSongFirebaseLoad = true;
                    checkFirebaseLoad();
                    getSingingInfo();
                } catch (Exception e) {
                    Log.e("getSentenceInfo", "?????? ????????? ????????? ????????? ??? ????????????.");
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

                        Log.v("?????????????????????", singingNoteDto.getStartTime());

                        singingNoteDtoList.add(singingNoteDto);
                    }
                    isSingingFirebaseLoad = true;
                    checkFirebaseLoad();
                } catch (Exception e) {
                    Log.e("getSingingInfo", "?????? ????????? ????????? ????????? ??? ????????????.");
                    Log.e("getSingingInfo", e.getMessage());
                }
            }
        });
    }

    private void checkFirebaseLoad() {
        Log.v("db ??????", isSongFirebaseLoad + " : " + isSingingFirebaseLoad);
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

    /* Mediaplayer ?????? */
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
                        Log.i("?????? ??????????????? ?????? ??????", e.getMessage());
                    }
                });
    }
}