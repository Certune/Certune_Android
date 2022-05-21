package com.techtown.tarsosdsp_pitchdetect.correction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.techtown.tarsosdsp_pitchdetect.PitchCorrectionListAdapter;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.CustomPitchCorrectionListDto;

public class RhythmCorrection extends AppCompatActivity {

    private ListView listView;
    private PitchCorrectionListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythm_correction);

        adapter = new PitchCorrectionListAdapter();

        listView = (ListView)findViewById(R.id.Ryythm_listView);
        listView.setAdapter(adapter);

        adapter.addItem("2/2 박자");
        adapter.addItem("2/4 박자");
        adapter.addItem("2/8 박자");
        adapter.addItem("3/2 박자");
        adapter.addItem("3/4 박자");
        adapter.addItem("3/8 박자");
        adapter.addItem("4/2 박자");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomPitchCorrectionListDto customPitchCorrectionListDto = (CustomPitchCorrectionListDto) parent.getItemAtPosition(position);

                String rhythm = customPitchCorrectionListDto.getOctave();
            }
        });
    }
}