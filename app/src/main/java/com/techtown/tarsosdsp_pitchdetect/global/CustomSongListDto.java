package com.techtown.tarsosdsp_pitchdetect.global;

import android.view.View;

import com.techtown.tarsosdsp_pitchdetect.SongListViewAdapter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomSongListDto {
    private String indexText; // TODO: index 가 Song 이라는 객체(class)의 고유한 속성(property, member variable)이라고 볼 수 없을 것 같아서, 그러면 여기서는 빠지는게 맞는 것 같아요.
    private String songText;
    private String singerText;
}
