package com.techtown.tarsosdsp_pitchdetect.score;

import java.util.ArrayList;

public class ProcessTimeRange {

    public static ArrayList<Double> processTimeRange(String startTime){
        Double low = Double.parseDouble(startTime)-0.3;
        Double high = Double.parseDouble(startTime)+0.3;

        ArrayList<Double> timeRangeList = new ArrayList<>();
        timeRangeList.add(low);
        timeRangeList.add(high);

        return timeRangeList;
    }

}