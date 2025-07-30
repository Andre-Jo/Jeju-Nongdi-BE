package com.jeju_nongdi.jeju_nongdi.dto.Chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "WebSocket 메시지 전송용 DTO")
public class SimpleMessagePayloadDTO {
    
    @NotBlank(message = "채팅방 ID는 필수입니다")
    @Schema(description = "채팅방 ID", example = "1_2", required = true)
    private String roomId;
    
    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다")
    @Schema(description = "메시지 내용", example = "안녕하세요!", required = true)
    private String content;
}
