package com.techtown.tarsosdsp_pitchdetect.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class MusicDto {

    private String cumul_time;
    private String note;
    private String time;

    @Builder
    public MusicDto(String cumul_time, String note, String time) {
        this.cumul_time = cumul_time;
        this.note = note;
        this.time = time;
    }
}
