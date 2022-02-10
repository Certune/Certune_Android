package com.techtown.tarsosdsp_pitchdetect;

import java.util.ArrayList;
import java.util.Arrays;

public class ProcessNoteRange {

    public static ArrayList<String> processNoteRange(String note) {
        if (note.equals("B3")) {
            String[] noteRangeList = {"A#3", "B3", "C4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("C4")) {
            String[] noteRangeList = {"B3", "C4", "C#4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("C#4")) {
            String[] noteRangeList = {"C4", "C#4", "D4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("D4")) {
            String[] noteRangeList = {"C#4", "D4", "D#4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("D#4")) {
            String[] noteRangeList = {"D4", "D#4", "E4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("E4")) {
            String[] noteRangeList = {"D#4", "E4", "F4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("F4")) {
            String[] noteRangeList = {"E4", "F4", "F#4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("F#4")) {
            String[] noteRangeList = {"F4", "F#4", "G4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("G4")) {
            String[] noteRangeList = {"F#4", "G4", "G#4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("G#4")) {
            String[] noteRangeList = {"G4", "G#4", "A4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("A4")) {
            String[] noteRangeList = {"G#4", "A4", "A#4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("A#4")) {
            String[] noteRangeList = {"A4", "A#4", "B4"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else if (note.equals("B4")) {
            String[] noteRangeList = {"A#4", "B4", "C5"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        } else {
            String[] noteRangeList = {"COM", "GONG", "WOW"};
            return new ArrayList<>(Arrays.asList(noteRangeList));
        }

    }
}
