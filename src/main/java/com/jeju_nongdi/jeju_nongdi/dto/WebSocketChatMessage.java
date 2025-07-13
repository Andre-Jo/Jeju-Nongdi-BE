package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketChatMessage {
    
    private String roomId;
    private String sender;
    private String content;
    private ChatMessage.MessageType type;
    private LocalDateTime timestamp;
    
    // Getter/Setter 메서드들
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public ChatMessage.MessageType getType() { return type; }
    public void setType(ChatMessage.MessageType type) { this.type = type; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public static WebSocketChatMessageBuilder builder() {
        return new WebSocketChatMessageBuilder();
    }
    
    public static class WebSocketChatMessageBuilder {
        private String roomId;
        private String sender;
        private String content;
        private ChatMessage.MessageType type;
        private LocalDateTime timestamp;
        
        public WebSocketChatMessageBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }
        
        public WebSocketChatMessageBuilder sender(String sender) {
            this.sender = sender;
            return this;
        }
        
        public WebSocketChatMessageBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public WebSocketChatMessageBuilder type(ChatMessage.MessageType type) {
            this.type = type;
            return this;
        }
        
        public WebSocketChatMessageBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public WebSocketChatMessage build() {
            WebSocketChatMessage message = new WebSocketChatMessage();
            message.setRoomId(roomId);
            message.setSender(sender);
            message.setContent(content);
            message.setType(type);
            message.setTimestamp(timestamp);
            return message;
        }
    }

    
    // 입장/퇴장 메시지 생성
    public static WebSocketChatMessage createEnterMessage(String roomId, String sender) {
        WebSocketChatMessage message = new WebSocketChatMessage();
        message.setRoomId(roomId);
        message.setSender(sender);
        message.setContent(sender + "님이 입장하셨습니다.");
        message.setType(ChatMessage.MessageType.ENTER);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
    
    public static WebSocketChatMessage createLeaveMessage(String roomId, String sender) {
        WebSocketChatMessage message = new WebSocketChatMessage();
        message.setRoomId(roomId);
        message.setSender(sender);
        message.setContent(sender + "님이 퇴장하셨습니다.");
        message.setType(ChatMessage.MessageType.LEAVE);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
}
