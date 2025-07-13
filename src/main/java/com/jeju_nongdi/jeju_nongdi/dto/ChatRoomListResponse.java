package com.jeju_nongdi.jeju_nongdi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListResponse {
    
    private List<ChatRoomResponse> chatRooms;
    private Long totalUnreadCount; // 전체 읽지 않은 메시지 수
    private int totalCount; // 전체 채팅방 수
    private int totalRooms; // 전체 채팅방 수 (totalCount와 동일)
    
    public static ChatRoomListResponse of(List<ChatRoomResponse> chatRooms, Long totalUnreadCount) {
        ChatRoomListResponse response = new ChatRoomListResponse();
        response.setChatRooms(chatRooms);
        response.setTotalUnreadCount(totalUnreadCount);
        response.setTotalCount(chatRooms.size());
        response.setTotalRooms(chatRooms.size());
        return response;
    }
    
    public static ChatRoomListResponseBuilder builder() {
        return new ChatRoomListResponseBuilder();
    }
    
    public static class ChatRoomListResponseBuilder {
        private List<ChatRoomResponse> chatRooms;
        private Long totalUnreadCount;
        private int totalCount;
        private int totalRooms;
        
        public ChatRoomListResponseBuilder chatRooms(List<ChatRoomResponse> chatRooms) {
            this.chatRooms = chatRooms;
            return this;
        }
        
        public ChatRoomListResponseBuilder totalUnreadCount(Long totalUnreadCount) {
            this.totalUnreadCount = totalUnreadCount;
            return this;
        }
        
        public ChatRoomListResponseBuilder totalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }
        
        public ChatRoomListResponseBuilder totalRooms(int totalRooms) {
            this.totalRooms = totalRooms;
            return this;
        }
        
        public ChatRoomListResponse build() {
            ChatRoomListResponse response = new ChatRoomListResponse();
            response.setChatRooms(chatRooms);
            response.setTotalUnreadCount(totalUnreadCount);
            response.setTotalCount(totalCount);
            response.setTotalRooms(totalRooms);
            return response;
        }
    }
}
