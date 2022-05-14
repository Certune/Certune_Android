package com.techtown.tarsosdsp_pitchdetect.score.logics;

import java.util.ArrayList;

public class CalcStartTimeRange {

    public static Boolean calcStartTimeRange(ArrayList<Double> timeRangeList, String userNoteStartTime){
        Double startTime = Double.parseDouble(userNoteStartTime);

        return timeRangeList.get(0) <= startTime && startTime <= timeRangeList.get(1);
    }
}
