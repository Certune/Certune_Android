package com.techtown.tarsosdsp_pitchdetect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.techtown.tarsosdsp_pitchdetect.CustomSongList;

import java.util.ArrayList;

public class SongListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<CustomSongList> listViewItemList = new ArrayList<CustomSongList>() ;

    // SongListViewAdapter 생성자
    public SongListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View 리턴
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder holder;

        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, null, false);

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new CustomViewHolder();
        holder.listIndex = (TextView) convertView.findViewById(R.id.indexTextView);
        holder.songTitle = (TextView) convertView.findViewById(R.id.songTextView);
        holder.singerName = (TextView) convertView.findViewById(R.id.singerTextView);

        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomSongList listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        holder.listIndex.setText(listViewItem.getIndexText());
        holder.songTitle.setText(listViewItem.getSongText());
        holder.singerName.setText(listViewItem.getSingerText());

        return convertView;
    }

    class CustomViewHolder {
        TextView listIndex;
        TextView songTitle;
        TextView singerName;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem(CustomSongList dto) {
        CustomSongList item = new CustomSongList();

        item.setIndexText(dto.getIndexText());
        item.setSongText(dto.getSongText());
        item.setSingerText(dto.getSingerText());

        listViewItemList.add(item);
    }
}
