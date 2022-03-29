package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MusicDto {

    private String startTime;
    private String endTime;
    private String lyrics;
    private ArrayList<NoteDto> notes;

    @Builder
    public MusicDto(String startTime, String endTime, String lyrics, ArrayList<NoteDto> notes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.lyrics = lyrics;
        this.notes = notes;
    }

}