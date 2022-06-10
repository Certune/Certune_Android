package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class SingingMusicDto {
    public String startTime;
    public String noteScore;
    public String rhythmScore;
    public String totalScore;
    public Boolean isPoor;
    public ArrayList<NoteDto> notes;

    public ArrayList<NoteDto> getNote() {
        return  notes;
    }
}
