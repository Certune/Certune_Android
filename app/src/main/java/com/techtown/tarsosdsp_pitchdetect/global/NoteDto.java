package com.techtown.tarsosdsp_pitchdetect.global;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NoteDto {

    private String startTime;
    private String endTime;
    private String note;

    @Builder
    public NoteDto(String startTime, String endTime, String note){
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
    }

}