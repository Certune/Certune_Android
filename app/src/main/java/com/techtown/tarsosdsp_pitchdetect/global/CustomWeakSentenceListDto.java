package com.techtown.tarsosdsp_pitchdetect.global;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomWeakSentenceListDto {
    String sentenceText;

    @Builder
    public CustomWeakSentenceListDto(){
        this.sentenceText = sentenceText;
    }
}