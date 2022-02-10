package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Data;

@Data
public class NoteDto {

    private String start_time;
    private String end_time;
    private String note;

    @Builder
    public NoteDto(String start_time, String end_time, String note) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.note = note;
    }
    @Builder
    public NoteDto(String start_time,String note){
        this.start_time=start_time;
        this.note = note;
    }


    public String getStart_time() {
        return start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public String getNote() {
        return note;
    }
}