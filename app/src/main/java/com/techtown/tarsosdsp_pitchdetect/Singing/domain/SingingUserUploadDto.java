package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import com.techtown.tarsosdsp_pitchdetect.MyRecord.domain.UserWeakMusicDto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SingingUserUploadDto {
    public ArrayList<SingingMusicDto> result;
    public String singerName;
    public String noteScore;
    public String rhythmScore;
    public String totalScore;
}
