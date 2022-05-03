package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TestMusicDto {

    private Double startTime;
    private Double endTime;
    private ArrayList<NoteDto> notes;

    @Builder
    public TestMusicDto(Double startTime, Double endTime, ArrayList<NoteDto> notes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

}
