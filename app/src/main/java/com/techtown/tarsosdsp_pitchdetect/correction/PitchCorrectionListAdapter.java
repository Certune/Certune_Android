package com.techtown.tarsosdsp_pitchdetect.correction;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;




import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.R;
import com.techtown.tarsosdsp_pitchdetect.global.CustomPitchCorrectionListDto;

import java.util.ArrayList;

public class PitchCorrectionListAdapter extends BaseAdapter  {
    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    private ArrayList<CustomPitchCorrectionListDto> pitchCorrectionLists = new ArrayList<>();

    public PitchCorrectionListAdapter() {
    }

    @Override
    public int getCount() {
        return pitchCorrectionLists.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder holder;

        final int pos = position;
        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pitchcorrectionlistview_item, null, false);
        }
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new CustomViewHolder();
        holder.pitch = (TextView) convertView.findViewById(R.id.pitchTextView);
        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomPitchCorrectionListDto listViewItem = pitchCorrectionLists.get(position);
        holder.pitch.setText(listViewItem.getOctave());

        return convertView;
    }

    class CustomViewHolder {
        TextView pitch;
    }

    @Override
    public CustomPitchCorrectionListDto getItem(int position){
        return pitchCorrectionLists.get(position);
    }

    public void addItem(String octave){
            CustomPitchCorrectionListDto customPitchCorrectionListDto = new CustomPitchCorrectionListDto();

            customPitchCorrectionListDto.setOctave(octave);

            pitchCorrectionLists.add(customPitchCorrectionListDto);
        }
    }



