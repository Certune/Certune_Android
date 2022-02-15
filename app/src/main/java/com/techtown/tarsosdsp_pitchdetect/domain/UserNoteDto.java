package com.techtown.tarsosdsp_pitchdetect.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class UserNoteDto {

    private String start_time;
    private String note;

    @Builder
    public UserNoteDto(String start_time, String note){
        this.start_time = start_time;
        this.note = note;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> faaed0dd8ca1e32bd1fc20af72b34bca492196c6
