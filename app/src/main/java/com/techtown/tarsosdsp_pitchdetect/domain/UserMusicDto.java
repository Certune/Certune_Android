package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Data;

@Data
public class UserMusicDto {
    public String start_time;
    public ArrayList<NoteDto> note;
    public String score;

    @Builder
    public UserMusicDto(String start_time, ArrayList<NoteDto> note, String score){
        this.start_time = start_time;
        this.note = note;
        this.score = score;
    }
}
