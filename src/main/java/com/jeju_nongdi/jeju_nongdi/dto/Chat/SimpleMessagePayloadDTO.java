package com.jeju_nongdi.jeju_nongdi.dto.Chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleMessagePayloadDTO {
    private String roomId;
    private String content;
}