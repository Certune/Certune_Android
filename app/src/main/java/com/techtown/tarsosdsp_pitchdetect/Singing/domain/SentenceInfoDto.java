package com.techtown.tarsosdsp_pitchdetect.Singing.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SentenceInfoDto {
    String startTime;
    String endTime;
    String lyrics;
}