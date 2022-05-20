package com.techtown.tarsosdsp_pitchdetect.global;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class CustomUserSongListDto {
    private String songText;
    private String singerText;
    private String totalScoreText;
    private String noteScoreText;
    private String rhythmScoreText;
}

