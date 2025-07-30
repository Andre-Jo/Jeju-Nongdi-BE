package com.jeju_nongdi.jeju_nongdi.dto.Chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "채팅방 목록 조회용 DTO (마지막 메시지 포함)")
public class ChatRoomView {
    
    @Schema(description = "채팅방 고유 ID", example = "1_2")
    private String roomId;
    
    @Schema(description = "첫 번째 사용자 ID", example = "1")
    private Long user1Id;
    
    @Schema(description = "두 번째 사용자 ID", example = "2")
    private Long user2Id;
    
    @Schema(description = "채팅방 생성 시간", example = "2025-07-30T15:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "마지막 메시지 내용", example = "안녕하세요!")
    private String lastMessageContent;
    
    @Schema(description = "마지막 메시지 시간", example = "2025-07-30T16:00:00")
    private LocalDateTime lastMessageTime;
    
    @Schema(description = "상대방 닉네임", example = "농부이씨")
    private String otherUserNickname;
    
    @Schema(description = "상대방 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String otherUserProfileImage;
}
