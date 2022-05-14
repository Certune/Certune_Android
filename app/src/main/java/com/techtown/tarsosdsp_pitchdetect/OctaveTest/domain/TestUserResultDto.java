package com.techtown.tarsosdsp_pitchdetect.OctaveTest.domain;

import com.techtown.tarsosdsp_pitchdetect.score.domain.UserMusicDto;

import java.util.ArrayList;

import lombok.Builder;

@Builder
public class TestUserResultDto {

    public ArrayList<UserMusicDto> songInfo;
    public String totalScore;
    public String noteScore;
    public String rhythmScore;

}
