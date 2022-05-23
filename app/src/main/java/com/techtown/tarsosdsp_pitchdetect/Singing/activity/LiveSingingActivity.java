package com.techtown.tarsosdsp_pitchdetect.Singing.activity;

import static com.techtown.tarsosdsp_pitchdetect.Singing.domain.NoteToIdx.noteToIdx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import androidx.gridlayout.widget.GridLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SentenceInfoDto;
import com.techtown.tarsosdsp_pitchdetect.Singing.domain.SingingNoteDto;
import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;

public class LiveSingingActivity extends AppCompatActivity {
    AudioDispatcher dispatcher;
    TarsosDSPAudioFormat tarsosDSPAudioFormat;

    LineChart mChart;

    private String userEmail;
    private Boolean isShifting;

    private String songName = "신호등";
    private String songLowKey = "D4";
    private String songHighKey = "B5";
    private Double songEndTime = 123.23;

    ArrayList<SingingNoteDto> noteDtoList = new ArrayList<>();
    ArrayList<SentenceInfoDto> sentenceInfoList = new ArrayList<>();

    private HorizontalScrollView scrollView;
    GridLayout gridLayout;
    Button cell;
    LoadingDialog dialog;

    int firstNoteStartTime;
    Long showStartTime;

    Boolean isNoteLoad = false;

    private static FirebaseFirestore database = FirebaseFirestore.getInstance();

    final Handler handlerSong = new Handler();
    final Handler handlerSetting = new Handler();
    final Handler handlerSetting2 = new Handler();

    final Runnable runnableSong = new Runnable() {
        @Override
        public void run() {
            setScrollSettings();
            for (SingingNoteDto noteDto : noteDtoList) {
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

    final Runnable runnableSetting = new Runnable() {
        @Override
        public void run() {
            for (int j = 0; j < gridLayout.getRowCount(); j++) {
                setCell(j, 0, 1, false, true);
            }
        }
    };

    final Runnable runnableSetting2 = new Runnable() {
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
        setContentView(R.layout.activity_live_singing);

        handlerSetting.post(runnableSetting);
        handlerSetting2.post(runnableSetting2);

        // Get xml instances
        mChart = findViewById(R.id.chart);
        scrollView = findViewById(R.id.horizontalScrollView);
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setUseDefaultMargins(false);

        dialog = new LoadingDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        // Basic Setting for tarsosDSP AudioFormat
        tarsosDSPAudioFormat = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        /*
        Intent subIntent = new Intent();
        songName = subIntent.getStringExtra("songName");
        userEmail = subIntent.getStringExtra("userEmail");
        songLowKey = subIntent.getStringExtra("songLowKey");
        songHighKey = subIntent.getStringExtra("songHighKey");
        isShifting = subIntent.getBooleanExtra("isShifting", false);
        */

       getSongEndTime();

//        microphoneOn();
//        setChart();
//        setAxis();

    }

    public void checkLoading() {
        // TODO : mp3 player 로딩 완료창도 추가
        if (isNoteLoad) {
            dialog.dismiss();
            showStartTime = System.currentTimeMillis();
        }
        final Handler handlerStart = new Handler();

        handlerStart.postDelayed(new Runnable() {
            @Override
            public void run() {
                setScrollSettings();
            }
        }, firstNoteStartTime * 1000L);
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

        cell = new Button(this);
        cell.setEnabled(false);
        cell.setLayoutParams(param);
        if (isNote) {
            cell.setBackgroundColor(Color.GRAY);
            Log.v("true일 때 색깔", "얍");
        }
        else {
            cell.setBackgroundColor(Color.BLUE); // 이거 없애면 버튼 홀쭉해짐
            Log.v("FALSE일 때 색깔", "얍");
        }
        if (isPrepare)
            cell.setBackgroundColor(Color.RED);

        gridLayout.addView(cell, param);
    }

    public void settingView() {
        Log.v("row 개수", String.valueOf(gridLayout.getRowCount()));
        gridLayout.setColumnCount((int) Math.round(songEndTime * 10));

        handlerSong.post(runnableSong);
    }

    private void setScrollSettings() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollX", 50000);
                Long durationTime = (long) (songEndTime * 1000); // TODO : SONG 길이에 맞춰서 넣어줘야 함
                objectAnimator.setDuration(durationTime);
                objectAnimator.setInterpolator(new LinearInterpolator());
                objectAnimator.start();
            }
        });
    }

    private void getSongEndTime() {

        database.collection("Singing").document("신호등")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
//                    try {
//                        songEndTime = Double.parseDouble((String) document.getData().get("endTime"));
                    songEndTime = 231.3;
                    List list = (List) Objects.requireNonNull(document.getData()).get("sentence");

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
                        sentenceInfoList.add(sentenceInfoDto);

                        // note 정보
                        ArrayList<HashMap<String, Object>> arrayMap = (ArrayList<HashMap<String, Object>>) map.get("notes");
                        for (HashMap<String, Object> notemap : arrayMap) {
                            SingingNoteDto noteDto = SingingNoteDto.builder()
                                    .startTime(String.valueOf(notemap.get("start_time")))
                                    .endTime(String.valueOf(notemap.get("end_time")))
                                    .note(String.valueOf(notemap.get("note")))
                                    .isNote((Boolean) notemap.get("isNote"))
                                    .build();

                            noteDtoList.add(noteDto);
                        }

                    }
                    settingView();

//                    } catch (Exception e) {
//                        Log.e("endTimeImport", e.getMessage());
//                    }
                }
            }
        });
    }

    public void addEntry(Float pitch) {
        LineData data = mChart.getData();

        if (data != null) {
            // Check that data has a set. If not, create and add a set to data
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), pitch + 1), 0);
            data.notifyDataChanged();

            mChart.notifyDataSetChanged();
            mChart.moveViewToX(data.getEntryCount());

        }
    }

    public ILineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);

        return set;
    }

    public void setChart() {
        mChart.setExtraBottomOffset(15f);
        mChart.setVisibleXRangeMaximum(10);
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        mChart.setScaleMinima(10f, 1f);
    }

    public void setAxis() {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setEnabled(false);

        YAxis leftYAxis = mChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f);
        leftYAxis.setAxisMaximum(200f);
        leftYAxis.setDrawAxisLine(false);
        leftYAxis.setEnabled(false);

        YAxis rightYAxis = mChart.getAxisRight();
        rightYAxis.setDrawAxisLine(false);
        rightYAxis.setEnabled(false);

        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.setDrawBorders(false);
    }

    public void microphoneOn() {
        releaseDispatcher();
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        PitchDetectionHandler pitchDetectionHandler = (res, e) -> {
            final float pitchInHz = res.getPitch();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pitchInHz > 40) {
                        addEntry(pitchInHz);
                    }
                }
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