package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserMusicDto {
    public String startTime;
    public String noteScore;
    public String rhythmScore;
    public String totalScore;
    public Boolean isPoor;
    public ArrayList<UserNoteDto> notes;

    @Builder
    public UserMusicDto(String startTime, String noteScore, String rhythmScore, String totalScore, Boolean isPoor, ArrayList<UserNoteDto> notes) {
        this.startTime = startTime;
        this.noteScore = noteScore;
        this.rhythmScore = rhythmScore;
        this.totalScore = totalScore;
        this.isPoor = isPoor;
        this.notes = notes;
    }
    
    public ArrayList<UserNoteDto> getNote() {
        return  notes;
    }
}