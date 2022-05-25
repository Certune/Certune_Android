package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import java.util.ArrayList;
import java.util.Arrays;

public class NoteToIdx {

    static ArrayList<String> noteList = new ArrayList<>(Arrays.asList(
            "F#6", "F6", "E6", "D#6", "D6", "C#6", "C6",
            "B#5", "B5", "A#5", "A5", "G#5", "G5", "F#5", "F5", "E5", "D#5", "D5", "C#5", "C5",
            "B#4", "B4", "A#4", "A4", "G#4", "G4", "F#4", "F4", "E4", "D#4", "D4", "C#4", "C4",
            "B#3", "B3", "A#3", "A3", "G#3", "G3"
    ));

    public static int noteToIdx(String songLowKey) {
        return noteList.indexOf(songLowKey);
    }
}