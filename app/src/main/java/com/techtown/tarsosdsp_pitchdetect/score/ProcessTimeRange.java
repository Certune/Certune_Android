package com.techtown.tarsosdsp_pitchdetect.score;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class ProcessTimeRange {

    public static ArrayList<Double> processTimeRange(String startTime){
        Double low=Double.parseDouble(startTime)-0.3;
        Double high=Double.parseDouble(startTime)+0.3;

        ArrayList<Double> timeRangeList = new ArrayList<>();
        timeRangeList.add(low);
        timeRangeList.add(high);

        return timeRangeList;
    }

}