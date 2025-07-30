package com.jeju_nongdi.jeju_nongdi.controller.Chat;

import com.jeju_nongdi.jeju_nongdi.dto.Chat.SimpleMessagePayloadDTO;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * STOMP 엔드포인트는 스프링 시큐리티의 WebSocket 메시지 보안 설정을 통해
     * AuthenticationPrincipal 을 주입받을 수 있습니다.
     */
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(
            @Payload SimpleMessagePayloadDTO payload,
            SimpMessageHeaderAccessor headerAccessor,
            @AuthenticationPrincipal User user) {

        if (user == null || payload.getRoomId() == null || payload.getContent() == null) {
            return;
        }
        chatService.saveAndBroadcastMessage(
                payload.getRoomId(),
                user.getEmail(),
                payload.getContent()
        );
    }
}
