package com.techtown.tarsosdsp_pitchdetect.MyRecord.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserWeakNoteDto {
    String startTime;
    String endTime;
    String note;

    @Builder
    public UserWeakNoteDto(String startTime, String endTime, String note){
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
    }
}
