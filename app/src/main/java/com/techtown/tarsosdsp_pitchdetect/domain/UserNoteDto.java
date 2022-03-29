package com.techtown.tarsosdsp_pitchdetect.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserNoteDto {

    private String startTime;
    private String note;

    @Builder
    public UserNoteDto(String startTime, String note){
        this.startTime = startTime;
        this.note = note;
    }

}



