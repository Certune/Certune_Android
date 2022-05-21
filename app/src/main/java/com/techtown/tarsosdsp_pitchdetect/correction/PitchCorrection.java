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

public class PitchCorrection extends AppCompatActivity {

    private ListView listView;
    private PitchCorrectionListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_correction);

        adapter = new PitchCorrectionListAdapter();

        listView = (ListView)findViewById(R.id.Pitch_listView);
        listView.setAdapter(adapter);

        adapter.addItem("C4 - C5");
        adapter.addItem("D4 - D5");
        adapter.addItem("E4 - E5");
        adapter.addItem("F4 - F5");
        adapter.addItem("G4 - G5");
        adapter.addItem("A4 - A5");
        adapter.addItem("B4 - B5");
        adapter.addItem("C5 - C6");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomPitchCorrectionListDto customPitchCorrectionListDto = (CustomPitchCorrectionListDto) parent.getItemAtPosition(position);

                String noteRange = customPitchCorrectionListDto.getOctave();
            }
        });
    }
}