package com.techtown.tarsosdsp_pitchdetect.MyRecord.domain;

import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;

import java.util.ArrayList;

import lombok.Builder;

@Builder
public class UserWeakMusicDto {
    String startTime;
    ArrayList<NoteDto> notes;
    String noteScore;
    String rhythmScore;
    String totalScore;

    public ArrayList<NoteDto> getNotes(){
        return notes;
    }
}
