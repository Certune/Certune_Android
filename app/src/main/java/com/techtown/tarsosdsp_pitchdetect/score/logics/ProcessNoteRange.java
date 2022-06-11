package com.techtown.tarsosdsp_pitchdetect.score.logics;

import java.util.ArrayList;
import java.util.Arrays;

public class ProcessNoteRange {

    public static ArrayList<String> processNoteRange(String note) {
        switch (note) {
            case "B3": {
                String[] noteRangeList = {"A3", "A#3", "B3", "C4", "C#4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "C4": {
                String[] noteRangeList = {"A#3", "B3", "C4", "C#4", "D4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "C#4": {
                String[] noteRangeList = {"B3", "C4", "C#4", "D4","D#4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "D4": {
                String[] noteRangeList = {"C4", "C#4", "D4", "D#4", "E4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "D#4": {
                String[] noteRangeList = {"C#4", "D4", "D#4", "E4", "F4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "E4": {
                String[] noteRangeList = {"D4","D#4", "E4", "F4", "F#4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "F4": {
                String[] noteRangeList = {"D#4", "E4", "F4", "F#4", "G4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "F#4": {
                String[] noteRangeList = {"E4", "F4", "F#4", "G4","G#4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "G4": {
                String[] noteRangeList = { "F4", "F#4", "G4", "G#4","A4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "G#4": {
                String[] noteRangeList = { "F#4", "G4", "G#4", "A4","A#4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "A4": {
                String[] noteRangeList = {"G4", "G#4", "A4", "A#4", "B4"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "A#4": {
                String[] noteRangeList = {"G#4", "A4", "A#4", "B4", "C5"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            case "B4": {
                String[] noteRangeList = {"A4", "A#4", "B4", "C5", "C#5"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
            default: {
                String[] noteRangeList = {"COM", "GONG", "YEAH"};
                return new ArrayList<>(Arrays.asList(noteRangeList));
            }
        }

    }
}
