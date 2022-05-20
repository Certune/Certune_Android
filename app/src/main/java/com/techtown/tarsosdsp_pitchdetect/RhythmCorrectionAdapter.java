package com.techtown.tarsosdsp_pitchdetect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.techtown.tarsosdsp_pitchdetect.global.CustomRhythmCorrectionListDto;

import java.util.ArrayList;

public class RhythmCorrectionAdapter extends BaseAdapter {

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    private ArrayList<CustomRhythmCorrectionListDto> rhythmCorrectionLists = new ArrayList<>();

    public RhythmCorrectionAdapter() {
    }

    @Override
    public int getCount() {
        return rhythmCorrectionLists.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RhythmCorrectionAdapter.CustomViewHolder holder;

        final int pos = position;
        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rhythmcorrectionlistview_item, null, false);
        }
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new RhythmCorrectionAdapter.CustomViewHolder();
        holder.rhythm = (TextView) convertView.findViewById(R.id.rhythmTextView);
        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomRhythmCorrectionListDto listViewItem = rhythmCorrectionLists.get(position);
        holder.rhythm.setText(listViewItem.getRhythm());

        return convertView;
    }

    class CustomViewHolder {
        TextView rhythm;
    }

    @Override
    public CustomRhythmCorrectionListDto getItem(int position){
        return rhythmCorrectionLists.get(position);
    }

    public void addItem(String rhythm){
        CustomRhythmCorrectionListDto customRhythmCorrectionListDto = new CustomRhythmCorrectionListDto();

        customRhythmCorrectionListDto.setRhythm(rhythm);
        rhythmCorrectionLists.add(customRhythmCorrectionListDto);
    }
}
