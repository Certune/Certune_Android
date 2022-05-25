package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SingingNoteDto {
    String startTime;
    String endTime;
    String note;
    Boolean isNote;

    @Builder
    public SingingNoteDto(String startTime, String endTime, String note, Boolean isNote){
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
        this.isNote = isNote;
    }

}