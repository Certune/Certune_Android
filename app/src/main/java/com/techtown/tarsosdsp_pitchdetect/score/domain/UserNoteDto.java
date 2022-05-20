package com.techtown.tarsosdsp_pitchdetect.score.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserNoteDto {

    private String startTime;
    private String note;

    @Builder
    public UserNoteDto(String startTime, String note){
        this.startTime = startTime;
        this.note = note;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> faaed0dd8ca1e32bd1fc20af72b34bca492196c6
