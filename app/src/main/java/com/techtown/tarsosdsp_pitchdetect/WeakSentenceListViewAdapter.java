package com.techtown.tarsosdsp_pitchdetect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.techtown.tarsosdsp_pitchdetect.CustomWeakSentenceList;
import com.techtown.tarsosdsp_pitchdetect.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class WeakSentenceListViewAdapter extends BaseAdapter implements View.OnClickListener {



    private ArrayList<CustomWeakSentenceList> listViewItemList = new ArrayList<CustomWeakSentenceList>();

    //WeakSentenceListViewAdapter 생성자
    public WeakSentenceListViewAdapter(){

    }

    //Adapter에 사용되는 데이터 개수 리턴
    @Override
    public int getCount(){
        return listViewItemList.size();
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder holder;

        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_weak_sentence_list, null, false);
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            holder = new CustomViewHolder();
            holder.sentence = (TextView) convertView.findViewById(R.id.sentenceTextView);



            convertView.setTag(holder);
        }else{
            holder = (CustomViewHolder) convertView.getTag();
        }

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CustomWeakSentenceList listViewItem = listViewItemList.get(position);
        final String text="연습하기";
        //button 클릭 시 TextView(textView1)의 내용 변경
        Button button = (Button)convertView.findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //button을 눌렀을 때 실행되는 함수

            }
        });
        // 아이템 내 각 위젯에 데이터 반영
        holder.sentence.setText(listViewItem.getSentenceText());


        return convertView;
    }


    @Override
    public void onClick(View v) {

    }

    class CustomViewHolder {
        TextView sentence;
        Button button;

    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem(CustomWeakSentenceList dto) {
        CustomWeakSentenceList item = new CustomWeakSentenceList();

        item.setSentenceText(dto.getSentenceText());

        listViewItemList.add(item);
    }

}
