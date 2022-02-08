package com.techtown.tarsosdsp_pitchdetect.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class NoteDto {

    private String start_time;
    private String end_time;
    private String note;

    @Builder
    public NoteDto(String start_time, String end_time, String note){
        this.start_time = start_time;
        this.end_time = end_time;
        this.note = note;
    }

    @Builder
    public NoteDto(String start_time, String note) {
        this.start_time = start_time;
        this.note = note;
    }

}