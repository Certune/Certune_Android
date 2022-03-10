package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserMusicDto {
    public String startTime;
    public ArrayList<UserNoteDto> notes;
    public String noteScore;
    public String rhythmScore;

    @Builder
    public UserMusicDto(String startTime, ArrayList<UserNoteDto> notes, String noteScore, String rhythmScore) {
        this.startTime = startTime;
        this.notes = notes;
        this.noteScore = noteScore;
        this.rhythmScore = rhythmScore;
    }
}
