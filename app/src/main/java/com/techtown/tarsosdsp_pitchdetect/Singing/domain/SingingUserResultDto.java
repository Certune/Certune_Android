package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import com.techtown.tarsosdsp_pitchdetect.score.domain.UserMusicDto;

import java.util.ArrayList;

import lombok.Builder;

@Builder
public class SingingUserResultDto {
    public ArrayList<UserMusicDto> songInfo;
    public String noteScore;
    public String rhythmScore;
}
