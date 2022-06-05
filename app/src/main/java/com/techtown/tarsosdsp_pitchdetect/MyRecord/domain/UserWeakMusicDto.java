package com.techtown.tarsosdsp_pitchdetect.MyRecord.domain;

import com.techtown.tarsosdsp_pitchdetect.score.domain.UserNoteDto;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWeakMusicDto {
    public String startTime;
    public String noteScore;
    public String rhythmScore;
    public String totalScore;
    public Boolean isPoor;
    public ArrayList<UserWeakNoteDto> notes;

    @Builder
    public UserWeakMusicDto(String startTime, String noteScore, String rhythmScore, String totalScore, Boolean isPoor, ArrayList<UserWeakNoteDto> notes) {
        this.startTime = startTime;
        this.noteScore = noteScore;
        this.rhythmScore = rhythmScore;
        this.totalScore = totalScore;
        this.isPoor = isPoor;
        this.notes = notes;
    }

    public ArrayList<UserWeakNoteDto> getNote() {
        return  notes;
    }
}