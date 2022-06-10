package com.techtown.tarsosdsp_pitchdetect;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.techtown.tarsosdsp_pitchdetect.Singing.activity.SingingStandbyActivity;
import com.techtown.tarsosdsp_pitchdetect.global.CustomSongListDto;

import java.util.ArrayList;

public class SongListViewAdapter extends BaseAdapter {

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    // TODO: ListView (RecyclerView) 각 row를 구성하는 요소를 전부 다 가지고 있는 List 보다는, 그런 구성요소를 만들기
    //  위해 필요한 '원본' 데이터를 가지고 있는게 좀 더 좋은 구조에요. 그래서 CustomSongListDto 에서 Index 값도 빠지는게
    //  좋아요. 왜냐하면 index는 사실 리스트에 순서를 표시하기 위한 임시적인 숫자이지, 노래(Song)라는 객체의 고유한 속성이라고
    //  볼 수는 없으니까요. 그런 맥락에서 변수명도 listViewItemList 보다는 songList 정도면 더 좋겠지요.
    private ArrayList<CustomSongListDto> listViewItemList = new ArrayList<CustomSongListDto>();

    // TODO: 위처럼 원본 데이터를 가지고 있으면, 그걸 songList 와 singerList 같은 형태로 다시 따로 저장해둘 필요는 없을
    //  것 같아요. 필요할 때는 DTO로부터 얻을 수 있으니까요.

    // SongListViewAdapter 생성자
    public SongListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View 리턴
    // TODO: 한번 더 정리하면, Adapter는 view를 만들기 위해 필요한 원본 데이터만 저장,
    //  getView() 함수를 이용해 원본 데이터로부터 view를 생성
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder holder;

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, null, false);

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        holder = new CustomViewHolder();
        holder.listIndex = (TextView) convertView.findViewById(R.id.indexTextView);
        holder.songTitle = (TextView) convertView.findViewById(R.id.songTextView);
        holder.singerName = (TextView) convertView.findViewById(R.id.singerTextView);
        holder.playBtn = (ImageButton) convertView.findViewById(R.id.playingButton);

        convertView.setTag(holder);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomSongListDto listViewItem = listViewItemList.get(position); // TODO: 리스트로 저장되어 있는 원본 데이터를 얻어서

        // TODO: view를 새롭게 그리면 끝.
        // 아이템 내 각 위젯에 데이터 반영
        holder.listIndex.setText(String.valueOf(position + 1));
        holder.songTitle.setText(listViewItem.getSongText());
        holder.singerName.setText(listViewItem.getSingerText());

        holder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("clicked", Integer.toString(position));

                if (holder.songTitle.getText().equals("신호등")) {
                    Intent intent = new Intent(v.getContext(), SingingStandbyActivity.class);
                    intent.putExtra("userEmail", holder.songTitle.getText());
                    intent.putExtra("songName", holder.songTitle.getText());
                    ((SongListActivity) v.getContext()).startActivity(intent);
                }
                else
                    Toast.makeText(v.getContext(), "현재는 신호등만 제공됩니다.", Toast.LENGTH_LONG).show();
            }
        });

        return convertView;
    }

    class CustomViewHolder {
        TextView listIndex;
        TextView songTitle;
        TextView singerName;
        ImageButton playBtn;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public CustomSongListDto getItem(int position) {
        return listViewItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem(CustomSongListDto dto) {
        listViewItemList.add(dto); // TODO: 원본 데이터를 리스트에 저장해두고
        notifyDataSetChanged(); // TODO: 이 함수로 원본 데이터에 수정이 생겼다고 adapter 에 알려줘요. 그러면 adapter가 다시 view를 그릴거에요.
    }
}