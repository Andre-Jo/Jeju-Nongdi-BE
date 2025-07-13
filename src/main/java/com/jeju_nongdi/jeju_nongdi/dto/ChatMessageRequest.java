package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    
    @NotBlank(message = "메시지 내용은 필수입니다")
    private String content;
    
    @Builder.Default
    private ChatMessage.MessageType messageType = ChatMessage.MessageType.CHAT;
    
    // 수동 getter 메서드들 - Lombok이 작동하지 않을 때를 위해
    public String getContent() { return content; }
    public ChatMessage.MessageType getMessageType() { return messageType; }
    
    public static ChatMessageRequestBuilder builder() {
        return new ChatMessageRequestBuilder();
    }
    
    public static class ChatMessageRequestBuilder {
        private String content;
        private ChatMessage.MessageType messageType = ChatMessage.MessageType.CHAT;
        
        public ChatMessageRequestBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public ChatMessageRequestBuilder messageType(ChatMessage.MessageType messageType) {
            this.messageType = messageType;
            return this;
        }
        
        public ChatMessageRequest build() {
            ChatMessageRequest request = new ChatMessageRequest();
            request.content = content;
            request.messageType = messageType;
            return request;
        }
    }

}
