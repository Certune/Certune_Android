package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import java.util.ArrayList;
import java.util.Arrays;

public class NoteToIdx {

    static ArrayList<String> noteList = new ArrayList<>(Arrays.asList(
            "C0", "C#0", "D0", "D#0", "E0", "E#0", "F0", "F#0", "G0", "G#0", "A0", "A#0", "B0", "B#0",
            "C1", "C#1", "D1", "D#1", "E1", "E#1", "F1", "F#1", "G1", "G#1", "A1", "A#1", "B1", "B#1",
            "C2", "C#2", "D2", "D#2", "E2", "E#2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2", "B#2",
            "C3", "C#3", "D3", "D#3", "E3", "E#3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3", "B#3",
            "C4", "C#4", "D4", "D#4", "E4", "E#4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4", "B#4",
            "C5", "C#5", "D5", "D#5", "E5", "E#5", "F5", "F#5", "G5", "G#5", "A5", "A#5", "B5", "B#5",
            "C6", "C#6", "D6", "D#6", "E6", "E#6", "F6", "F#6", "G6", "G#6", "A6", "A#6", "B6", "B#6",
            "C7", "C#7", "D7", "D#7", "E7", "E#7", "F7", "F#7", "G7", "G#7", "A7", "A#7", "B7", "B#7"
    ));

    public static int noteToIdx(String songLowKey) {
        return noteList.indexOf(songLowKey);
    }
}