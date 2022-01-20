package com.techtown.tarsosdsp_pitchdetect.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class UserMusicDto {
    public String start_time;
    public String end_time;
    public String note;
    public String grade;

    @Builder
    public UserMusicDto(String cumul_time, String note){
        this.start_time = start_time;
        this.end_time = end_time;
        this.note = note;
        this.grade = grade;
    }
}
