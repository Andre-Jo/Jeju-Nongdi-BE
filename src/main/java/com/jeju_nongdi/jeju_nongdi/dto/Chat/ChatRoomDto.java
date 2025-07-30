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
@Schema(description = "채팅방 정보 응답 DTO")
public class ChatRoomDto {
    
    @Schema(description = "채팅방 고유 ID", example = "1_2")
    private String roomId;
    
    @Schema(description = "첫 번째 사용자 ID (작은 ID)", example = "1")
    private Long user1Id;
    
    @Schema(description = "두 번째 사용자 ID (큰 ID)", example = "2")
    private Long user2Id;
    
    @Schema(description = "채팅방 생성 시간", example = "2025-07-30T15:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "상대방 닉네임", example = "농부이씨")
    private String otherUserNickname;
    
    @Schema(description = "상대방 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String otherUserProfileImage;
}
