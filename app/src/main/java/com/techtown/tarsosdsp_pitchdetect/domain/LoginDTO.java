package com.techtown.tarsosdsp_pitchdetect.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class LoginDTO {
    private String gender;
    private String nickname;
}
