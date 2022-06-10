package com.techtown.tarsosdsp_pitchdetect.global;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongSentenceDto {

    Double sentenceStartTime;
    Double sentenceEndTime;
    Double sentenceNoteEndTime;
    ArrayList<NoteDto> sentenceNoteDtoList;
    Double sentenceDurationTime;
    Integer sentenceNoteNum;

}
