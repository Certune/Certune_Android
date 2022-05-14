package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SingingUserUploadDto {
    public SingingUserResultDto result;
    public String singerName;
    public String totalScore;
}
