package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;

public class UserSongInfoDto {

    public ArrayList<UserMusicDto> songInfo;
    public String totalScore;
    public String noteScore;
    public String rhythmScore;

    @Builder
    public UserSongInfoDto(ArrayList<UserMusicDto> songInfo, String totalScore, String noteScore, String rhythmScore) {
        this.songInfo = songInfo;
        this.totalScore = totalScore;
        this.noteScore = noteScore;
        this.rhythmScore = rhythmScore;
    }
}
