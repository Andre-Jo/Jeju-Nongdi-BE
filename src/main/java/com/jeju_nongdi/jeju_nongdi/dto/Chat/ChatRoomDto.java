package com.jeju_nongdi.jeju_nongdi.dto.Chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private String roomId;
    private Long user1Id;
    private Long user2Id;
    private LocalDateTime createdAt;
    private String otherUserNickname;
    private String otherUserProfileImage;
}