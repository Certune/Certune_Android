package com.techtown.tarsosdsp_pitchdetect.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class UserMusicDto {
    public String cumul_time;
    public String note;

    @Builder
    public UserMusicDto(String cumul_time, String note){
        this.cumul_time = cumul_time;
        this.note = note;
    }
}
