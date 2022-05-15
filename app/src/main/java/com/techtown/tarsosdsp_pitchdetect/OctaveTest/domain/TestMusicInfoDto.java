package com.techtown.tarsosdsp_pitchdetect.OctaveTest.domain;

import com.techtown.tarsosdsp_pitchdetect.global.NoteDto;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TestMusicInfoDto {

    private Double startTime;
    private Double endTime;
    private ArrayList<NoteDto> notes;

    @Builder
    public TestMusicInfoDto(Double startTime, Double endTime, ArrayList<NoteDto> notes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

}
