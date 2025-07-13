package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.ChatMessage;
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
public class ChatMessageResponse {
    
    private Long id;
    private String roomId;
    private UserResponse sender;
    private String content;
    private ChatMessage.MessageType messageType;
    private String messageTypeName;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private Boolean isMine; // 내가 보낸 메시지인지 여부
    
    public static ChatMessageResponse from(ChatMessage message, User currentUser) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setRoomId(message.getChatRoom().getRoomId());
        response.setSender(UserResponse.from(message.getSender()));
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setMessageTypeName(message.getMessageType().getKoreanName());
        response.setIsRead(message.getIsRead());
        response.setSentAt(message.getSentAt());
        response.setIsMine(message.getSender().equals(currentUser));
        return response;
    }
    
    public static ChatMessageResponseBuilder builder() {
        return new ChatMessageResponseBuilder();
    }
    
    public static class ChatMessageResponseBuilder {
        private Long id;
        private String roomId;
        private UserResponse sender;
        private String content;
        private ChatMessage.MessageType messageType;
        private String messageTypeName;
        private Boolean isRead;
        private LocalDateTime sentAt;
        private Boolean isMine;
        
        public ChatMessageResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public ChatMessageResponseBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }
        
        public ChatMessageResponseBuilder sender(UserResponse sender) {
            this.sender = sender;
            return this;
        }
        
        public ChatMessageResponseBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public ChatMessageResponseBuilder messageType(ChatMessage.MessageType messageType) {
            this.messageType = messageType;
            return this;
        }
        
        public ChatMessageResponseBuilder messageTypeName(String messageTypeName) {
            this.messageTypeName = messageTypeName;
            return this;
        }
        
        public ChatMessageResponseBuilder isRead(Boolean isRead) {
            this.isRead = isRead;
            return this;
        }
        
        public ChatMessageResponseBuilder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }
        
        public ChatMessageResponseBuilder isMine(Boolean isMine) {
            this.isMine = isMine;
            return this;
        }
        
        public ChatMessageResponse build() {
            ChatMessageResponse response = new ChatMessageResponse();
            response.setId(id);
            response.setRoomId(roomId);
            response.setSender(sender);
            response.setContent(content);
            response.setMessageType(messageType);
            response.setMessageTypeName(messageTypeName);
            response.setIsRead(isRead);
            response.setSentAt(sentAt);
            response.setIsMine(isMine);
            return response;
        }
    }
}
