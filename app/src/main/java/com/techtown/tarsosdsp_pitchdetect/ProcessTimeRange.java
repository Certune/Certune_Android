package com.techtown.tarsosdsp_pitchdetect;

import java.util.ArrayList;
import java.util.Arrays;

public class ProcessTimeRange {

    public static ArrayList<String> processTimeRange(String startTime){
        Double low=Double.parseDouble(startTime)-0.3;
        Double high=Double.parseDouble(startTime)+0.3;
        //  Log.v("TAG",String.valueOf(low));
        //  Log.v("TAG",String.valueOf(high));
//        if(Double.parseDouble(startTime)>=low && Double.parseDouble(startTime)<=high){
//            String [] timeRangeList={String.valueOf(low),startTime,String.valueOf(high)};
//            return new ArrayList<>(Arrays.asList(timeRangeList));
//        }
//        return null;
        String [] timeRangeList={String.valueOf(low),startTime,String.valueOf(high)};
        return new ArrayList<>(Arrays.asList(timeRangeList));
    }

}