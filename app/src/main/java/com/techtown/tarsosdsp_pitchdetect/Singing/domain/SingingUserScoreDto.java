package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SingingUserScoreDto {
    public String totalScore;
    public String noteScore;
    public String rhythmScore;
}
