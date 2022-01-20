package com.techtown.tarsosdsp_pitchdetect.domain;

import com.techtown.tarsosdsp_pitchdetect.domain.NoteDto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class MusicDto {

    private ArrayList<String> sentences;
    private String start_time;
    private String end_time;
    private String lyrics;
    private ArrayList<NoteDto> notes;

    @Builder
    public MusicDto(String start_time, String end_time, String lyrics, ArrayList<NoteDto> notes) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.lyrics = lyrics;
        this.notes = notes;
    }

}