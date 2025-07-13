package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.ChatRoom;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateRequest {
    
    @NotNull(message = "채팅 타입은 필수입니다")
    private ChatRoom.ChatType chatType;
    
    @NotNull(message = "참조 ID는 필수입니다")
    private Long referenceId; // 멘토링 ID 또는 농지 ID
    
    @NotNull(message = "상대방 ID는 필수입니다")
    private Long participantId; // 채팅 상대방 사용자 ID
    
    private String initialMessage; // 첫 메시지 (선택사항)
    
    // 수동 getter 메서드들 - Lombok이 작동하지 않을 때를 위해
    public ChatRoom.ChatType getChatType() { return chatType; }
    public Long getReferenceId() { return referenceId; }
    public Long getParticipantId() { return participantId; }
    public String getInitialMessage() { return initialMessage; }
}
