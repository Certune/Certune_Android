package com.techtown.tarsosdsp_pitchdetect;

import android.graphics.drawable.Drawable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomWeakSentenceList {
    private String sentenceText;
    private String songText;
    private String singerText;

    public CustomWeakSentenceList(){

    }
    public CustomWeakSentenceList(String sentenceText){
        this.sentenceText=sentenceText;
    }

    public void setSentenceText(String text) {
        sentenceText = text;
    }

    public String getSentenceText() {
        return sentenceText;
    }


}
