package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

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
    @Builder
    public MusicDto(String start_time,ArrayList<NoteDto> notes){
        this.start_time=start_time;
        this.notes=notes;
    }


    public String getStart_time() {
        return start_time;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getEnd_time() {
        return end_time;
    }

    public ArrayList<NoteDto> getNotes() {
        return notes;
    }
}