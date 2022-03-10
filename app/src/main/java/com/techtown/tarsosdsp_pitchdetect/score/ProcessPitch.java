package com.techtown.tarsosdsp_pitchdetect.score;

public class ProcessPitch {
    public static String processPitch(float pitchInHz){
        String noteText = "Nope";
        if(pitchInHz >= 16.35 && pitchInHz < 17.32) {
            noteText = "C0";
        }
        else if(pitchInHz >= 17.32 && pitchInHz < 18.35) {
            noteText = "C#0";
        }
        else if(pitchInHz >= 18.35 && pitchInHz < 19.45) {
            noteText = "D0";
        }
        else if(pitchInHz >= 19.45 && pitchInHz < 20.60) {
            noteText = "D#0";
        }
        else if(pitchInHz >= 20.60 && pitchInHz <= 21.83) {
            noteText = "E0";
        }
        else if(pitchInHz >= 21.83 && pitchInHz < 23.12) {
            noteText = "F0";
        }
        else if(pitchInHz >= 23.12 && pitchInHz < 24.50) {
            noteText = "F#0";
        }
        else if(pitchInHz >= 24.50 && pitchInHz < 25.96) {
            noteText = "G0";
        }
        else if(pitchInHz >= 25.96 && pitchInHz < 27.50) {
            noteText = "G#0";
        }
        else if(pitchInHz >= 27.50 && pitchInHz < 29.14) {
            noteText = "A0";
        }
        else if(pitchInHz >= 29.14 && pitchInHz < 30.87) {
            noteText = "A#0";
        }
        else if(pitchInHz >= 30.87 && pitchInHz < 32.70) {
            noteText = "B0";
        }
        else if(pitchInHz >= 32.70 && pitchInHz < 34.65) {
            noteText = "C1";
        }
        else if(pitchInHz >= 34.65 && pitchInHz < 36.71) {
            noteText = "C#1";
        }
        else if(pitchInHz >= 36.71 && pitchInHz < 38.89) {
            noteText = "D1";
        }
        else if(pitchInHz >= 38.89 && pitchInHz < 41.20) {
            noteText = "D#1";
        }
        else if(pitchInHz >= 41.20 && pitchInHz < 43.65) {
            noteText = "E1";
        }
        else if(pitchInHz >= 43.65 && pitchInHz < 46.25) {
            noteText = "F1";
        }
        else if(pitchInHz >= 46.25 && pitchInHz < 49.00) {
            noteText = "F#1";
        }
        else if(pitchInHz >= 49.00 && pitchInHz < 51.91) {
            noteText = "G1";
        }
        else if(pitchInHz >= 51.91 && pitchInHz < 55.00) {
            noteText = "G#1";
        }
        else if(pitchInHz >= 55.00 && pitchInHz < 58.27) {
            noteText = "A1";
        }
        else if(pitchInHz >= 58.27 && pitchInHz < 61.74) {
            noteText = "A#1";
        }
        else if(pitchInHz >= 61.74 && pitchInHz < 65.41) {
            noteText = "B1";
        }
        else if(pitchInHz >= 65.41 && pitchInHz < 69.30) {
            noteText = "C2";
        }
        else if(pitchInHz >= 69.30 && pitchInHz < 73.42) {
            noteText = "C#2";
        }
        else if(pitchInHz >= 73.42 && pitchInHz < 77.78) {
            noteText = "D2";
        }
        else if(pitchInHz >= 77.78 && pitchInHz < 82.41) {
            noteText = "D#2";
        }
        else if(pitchInHz >= 82.41 && pitchInHz < 87.31) {
            noteText = "E2";
        }
        else if(pitchInHz >= 87.31 && pitchInHz < 92.50) {
            noteText = "F2";
        }
        else if(pitchInHz >= 92.50 && pitchInHz < 98.00) {
            noteText = "F#2";
        }
        else if(pitchInHz >= 98.00 && pitchInHz < 103.83) {
            noteText = "G2";
        }
        else if(pitchInHz >= 103.83 && pitchInHz < 110.00) {
            noteText = "G#2";
        }
        else if(pitchInHz >= 110.00 && pitchInHz < 116.54) {
            noteText = "A2";
        }
        else if(pitchInHz >= 116.54 && pitchInHz < 123.47) {
            noteText = "A#2";
        }
        else if(pitchInHz >= 123.47 && pitchInHz < 130.81) {
            noteText = "B2";
        }
        else if(pitchInHz >= 130.81 && pitchInHz < 138.59) {
            noteText = "C3";
        }
        else if(pitchInHz >= 138.59 && pitchInHz < 146.83) {
            noteText = "C#3";
        }
        else if(pitchInHz >= 146.83 && pitchInHz < 155.56) {
            noteText = "D3";
        }
        else if(pitchInHz >= 155.56 && pitchInHz < 164.81) {
            noteText = "D#3";
        }
        else if(pitchInHz >= 164.81 && pitchInHz < 174.61) {
            noteText = "E3";
        }
        else if(pitchInHz >= 174.61 && pitchInHz < 185.00) {
            noteText = "F3";
        }
        else if(pitchInHz >= 185.00 && pitchInHz < 196.00) {
            noteText = "F#3";
        }
        else if(pitchInHz >= 196.00 && pitchInHz < 207.65) {
            noteText = "G3";
        }
        else if(pitchInHz >= 207.65 && pitchInHz < 220.00) {
            noteText = "G#3";
        }
        else if(pitchInHz >= 220.00 && pitchInHz < 233.08) {
            noteText = "A3";
        }
        else if(pitchInHz >= 233.08 && pitchInHz < 246.94) {
            noteText = "A#3";
        }
        else if(pitchInHz >= 246.94 && pitchInHz < 261.63) {
            noteText = "B3";
        }
        else if(pitchInHz >= 261.63 && pitchInHz < 277.18) {
            noteText = "C4";
        }
        else if(pitchInHz >= 277.18 && pitchInHz < 293.66) {
            noteText = "C#4";
        }
        else if(pitchInHz >= 293.66 && pitchInHz < 311.13) {
            noteText = "D4";
        }
        else if(pitchInHz >= 311.13 && pitchInHz < 329.63) {
            noteText = "D#4";
        }
        else if(pitchInHz >= 329.63 && pitchInHz < 349.23) {
            noteText = "E4";
        }
        else if(pitchInHz >= 349.23 && pitchInHz < 369.99) {
            noteText = "F4";
        }
        else if(pitchInHz >= 369.99 && pitchInHz < 392.00) {
            noteText = "F#4";
        }
        else if(pitchInHz >= 392.00 && pitchInHz < 415.30) {
            noteText = "G4";
        }
        else if(pitchInHz >= 415.30 && pitchInHz < 440.00) {
            noteText = "G#4";
        }
        else if(pitchInHz >= 440.00 && pitchInHz < 466.16) {
            noteText = "A4";
        }
        else if(pitchInHz >= 466.16 && pitchInHz < 493.88) {
            noteText = "A#4";
        }
        else if(pitchInHz >= 493.88 && pitchInHz < 523.25) {
            noteText = "B4";
        }
        else if(pitchInHz >= 523.25 && pitchInHz < 554.37) {
            noteText = "C5";
        }
        else if(pitchInHz >= 554.37 && pitchInHz < 587.33) {
            noteText = "C#5";
        }
        else if(pitchInHz >= 587.33 && pitchInHz < 622.25) {
            noteText = "D5";
        }
        else if(pitchInHz >= 622.25 && pitchInHz < 659.25) {
            noteText = "D#5";
        }
        else if(pitchInHz >= 659.25 && pitchInHz < 698.46) {
            noteText = "E5";
        }
        else if(pitchInHz >= 698.46 && pitchInHz < 739.99) {
            noteText = "F5";
        }
        else if(pitchInHz >= 739.99 && pitchInHz < 783.99) {
            noteText = "F#5";
        }
        else if(pitchInHz >= 783.99 && pitchInHz < 830.61) {
            noteText = "G5";
        }
        else if(pitchInHz >= 830.61 && pitchInHz < 880.00) {
            noteText = "G#5";
        }
        else if(pitchInHz >= 880.00 && pitchInHz < 932.33) {
            noteText = "A5";
        }
        else if(pitchInHz >= 932.33 && pitchInHz < 987.77) {
            noteText = "A#5";
        }
        else if(pitchInHz >= 987.77 && pitchInHz < 1046.50) {
            noteText = "B5";
        }
        else if(pitchInHz >= 1046.50 && pitchInHz < 1108.73) {
            noteText = "C6";
        }
        else if(pitchInHz >= 1108.73 && pitchInHz < 1174.66) {
            noteText = "C#6";
        }
        else if(pitchInHz >= 1174.66 && pitchInHz < 1244.51) {
            noteText = "D6";
        }
        else if(pitchInHz >= 1244.51 && pitchInHz < 1318.51) {
            noteText = "D#6";
        }
        else if(pitchInHz >= 1318.51 && pitchInHz < 622.25) {
            noteText = "E6";
        }
        else if (pitchInHz >=1396.91 && pitchInHz< 1479.98) {
            noteText = "F6";
        }
        else if (pitchInHz >=1174.66 && pitchInHz< 1567.98) {
            noteText = "F#6";
        }
        else if (pitchInHz >=1567.98 && pitchInHz< 1661.22) {
            noteText = "G6";
        }
        else if (pitchInHz >=1661.22 && pitchInHz< 1760.00) {
            noteText = "G#6";
        }
        else if (pitchInHz >=1760.00 && pitchInHz< 1864.66) {
            noteText = "A6";
        }
        else if (pitchInHz >=1864.66 && pitchInHz< 1975.53) {
            noteText = "A#6";
        }
        else if (pitchInHz >=1975.53 && pitchInHz< 2093.00) {
            noteText = "B6";
        }
        else if (pitchInHz >=2093.00 && pitchInHz< 2217.46) {
            noteText = "C7";
        }
        else if (pitchInHz >=2217.46 && pitchInHz< 2349.32) {
            noteText = "C#7";
        }
        else if (pitchInHz >=2349.32 && pitchInHz< 2489.02) {
            noteText = "D7";
        }
        else if (pitchInHz >=2489.02 && pitchInHz < 2637.02) {
            noteText = "D#7";
        }
        else if (pitchInHz >=2637.02 && pitchInHz < 2793.83) {
            noteText = "E7";
        }
        else if (pitchInHz >=2793.83 && pitchInHz < 2959.96) {
            noteText = "F7";
        }
        else if (pitchInHz >=2959.96 && pitchInHz < 3135.96) {
            noteText = "F#7";
        }
        else if(pitchInHz >= 3135.96 && pitchInHz < 3322.44) {
            noteText = "G7";
        }
        else if(pitchInHz >= 3322.44 && pitchInHz < 3520.00	) {
            noteText = "G#7";
        }
        else if(pitchInHz >= 3520.00 && pitchInHz < 3729.31) {
            noteText = "A7";
        }
        else if(pitchInHz >= 3729.31 && pitchInHz < 3951.07) {
            noteText = "A#7";
        }
        else if(pitchInHz >= 3951.07 && pitchInHz < 4186.01) {
            noteText = "B7";
        }
        else if(pitchInHz >= 4186.01 && pitchInHz < 4434.92) {
            noteText = "C8";
        }
        else if(pitchInHz >= 4434.92 && pitchInHz < 4698.63) {
            noteText = "C#8";
        }
        else if(pitchInHz >= 4698.63 && pitchInHz < 4978.03) {
            noteText = "D8";
        }
        else if(pitchInHz >= 4978.03 && pitchInHz < 5274.04) {
            noteText = "D#8";
        }
        else if(pitchInHz >= 5274.04 && pitchInHz < 5587.65) {
            noteText = "E8";
        }
        else if(pitchInHz >= 5587.65 && pitchInHz < 5919.91) {
            noteText = "F8";
        }
        else if(pitchInHz >= 5919.91 && pitchInHz < 6271.93) {
            noteText = "F#8";
        }
        else if(pitchInHz >= 6271.93 && pitchInHz < 6644.88) {
            noteText = "G8";
        }
        else if(pitchInHz >= 6644.88 && pitchInHz < 7040.00) {
            noteText = "G#8";
        }
        else if(pitchInHz >= 7040.00 && pitchInHz < 7458.62) {
            noteText = "A8";
        }
        else if(pitchInHz >= 7458.62 && pitchInHz < 7902.13) {
            noteText = "A#8";
        }
        else if(pitchInHz >= 7902.13) {
            noteText = "B8";
        }

        return noteText;
    }
}
