package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.ChatRoom;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    
    private Long id;
    private String roomId;
    private String title;
    private ChatRoom.ChatType chatType;
    private String chatTypeName;
    private Long referenceId;
    private UserResponse creator;
    private UserResponse participant;
    private UserResponse otherParticipant; // 현재 사용자가 아닌 상대방
    private Boolean isActive;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount; // 읽지 않은 메시지 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 참조 정보 (멘토링 제목, 농지 제목 등)
    private String referenceTitle;
    private String referenceType;
    
    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public void setOtherParticipant(UserResponse otherParticipant) {
        this.otherParticipant = otherParticipant;
    }
    
    public static ChatRoomResponse from(ChatRoom chatRoom, User currentUser) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.setId(chatRoom.getId());
        response.setRoomId(chatRoom.getRoomId());
        response.setTitle(chatRoom.getTitle());
        response.setChatType(chatRoom.getChatType());
        response.setChatTypeName(chatRoom.getChatType().getKoreanName());
        response.setReferenceId(chatRoom.getReferenceId());
        response.setCreator(UserResponse.from(chatRoom.getCreator()));
        response.setParticipant(UserResponse.from(chatRoom.getParticipant()));
        response.setOtherParticipant(UserResponse.from(chatRoom.getOtherParticipant(currentUser)));
        response.setIsActive(chatRoom.getIsActive());
        response.setLastMessage(chatRoom.getLastMessage());
        response.setLastMessageTime(chatRoom.getLastMessageTime());
        response.setCreatedAt(chatRoom.getCreatedAt());
        response.setUpdatedAt(chatRoom.getUpdatedAt());
        return response;
    }
}
