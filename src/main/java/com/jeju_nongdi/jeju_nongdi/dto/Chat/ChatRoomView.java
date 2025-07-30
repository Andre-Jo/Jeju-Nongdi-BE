package com.jeju_nongdi.jeju_nongdi.dto.Chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomView {
    private String roomId;
    private Long user1Id;
    private Long user2Id;
    private LocalDateTime createdAt;
    private String lastMessageContent; // 마지막 메시지 내용
    private LocalDateTime lastMessageTime; // 마지막 메시지 시간

    private String otherUserNickname;
    private String otherUserProfileImage;
}