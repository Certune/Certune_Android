package com.techtown.tarsosdsp_pitchdetect.domain;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SongSentenceDto {

    Double sentenceStartTime;
    Double sentenceEndTime;
    ArrayList<NoteDto> sentenceNoteDtoList;
    Double sentenceDurationTime;
    Integer sentenceNoteNum;

    @Builder
    public SongSentenceDto(Double sentenceStartTime, Double sentenceEndTime, ArrayList<NoteDto> sentenceNoteDtoList,
                           Double sentenceDurationTime, Integer sentenceNoteNum){
        this.sentenceStartTime = sentenceStartTime;
        this.sentenceEndTime = sentenceEndTime;
        this.sentenceNoteDtoList = sentenceNoteDtoList;
        this.sentenceDurationTime = sentenceDurationTime;
        this.sentenceNoteNum = sentenceNoteNum;
    }
}
