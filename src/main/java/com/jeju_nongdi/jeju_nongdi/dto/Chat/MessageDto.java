package com.jeju_nongdi.jeju_nongdi.dto.Chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 메시지 응답 DTO")
public class MessageDto {
    
    @Schema(description = "메시지 고유 ID", example = "1")
    private Long id;
    
    @Schema(description = "채팅방 ID", example = "1_2")
    private String roomId;
    
    @Schema(description = "발신자 사용자 ID", example = "1")
    private Long senderId;
    
    @Schema(description = "수신자 사용자 ID", example = "2")
    private Long receiverId;
    
    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;
    
    @Schema(description = "메시지 생성 시간", example = "2025-07-30T15:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "발신자 닉네임", example = "농부김씨")
    private String senderNickname;
    
    @Schema(description = "발신자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String senderProfileImage;
}
