package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import com.techtown.tarsosdsp_pitchdetect.score.domain.UserMusicDto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SingingUserUploadDto {
    public List<UserMusicDto> result;
    public String singerName;
    public String noteScore;
    public String rhythmScore;
    public String totalScore;
}
