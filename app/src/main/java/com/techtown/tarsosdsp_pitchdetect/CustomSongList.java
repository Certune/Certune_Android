package com.techtown.tarsosdsp_pitchdetect;

public class CustomSongList {
    private String indexText;
    private String songText;
    private String singerText;

    public void setIndexText(String text) { indexText = text; }

    public void setSongText(String text) {
        songText = text;
    }

    public void setSingerText(String text) {
        singerText = text;
    }

    public String getIndexText() { return indexText; }

    public String getSongText() {
        return songText;
    }

    public String getSingerText() {
        return singerText;
    }
}
