package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class UserMusicDto {
    public String start_time;
    public ArrayList<UserNoteDto> notes;
    public String score;

    @Builder
    public UserMusicDto(String start_time, ArrayList<UserNoteDto> notes, String score) {
        this.start_time = start_time;
        this.notes = notes;
        this.score = score;
    }
}
