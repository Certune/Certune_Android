package com.techtown.tarsosdsp_pitchdetect.global;

import lombok.Getter;

@Getter
public class SongTotalNoteTimeDto {

    int songTotalNoteNum = 0;
    double songTotalSentenceTime = 0;

    public void addSongTotalNoteNum(int songTotalNoteNum){
        this.songTotalNoteNum += songTotalNoteNum;
    }

    public void addSongTotalSentenceTime(double songTotalSentenceTime){
        this.songTotalSentenceTime += songTotalSentenceTime;
    }
}
