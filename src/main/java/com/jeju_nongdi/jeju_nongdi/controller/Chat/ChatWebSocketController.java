package com.jeju_nongdi.jeju_nongdi.controller.Chat;

import com.jeju_nongdi.jeju_nongdi.dto.Chat.SimpleMessagePayloadDTO;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * WebSocket 기반 실시간 채팅 컨트롤러
 * 
 * STOMP 프로토콜을 사용하여 실시간 메시지 전송을 처리합니다.
 * 
 * WebSocket 연결 엔드포인트: /api/ws
 * 메시지 전송 주소: /app/chat.sendPrivateMessage
 * 구독 주소: /topic/chat/room/{roomId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * 1:1 채팅 메시지 전송
     * 
     * @param payload 메시지 내용과 채팅방 ID
     * @param headerAccessor WebSocket 헤더 정보
     * @param user 인증된 사용자 정보
     */
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(
            @Payload @Valid SimpleMessagePayloadDTO payload,
            SimpMessageHeaderAccessor headerAccessor,
            @AuthenticationPrincipal User user) {

        // 기본 유효성 검사
        if (user == null) {
            log.warn("인증되지 않은 사용자의 메시지 전송 시도");
            return;
        }

        if (payload == null || !StringUtils.hasText(payload.getRoomId()) || 
            !StringUtils.hasText(payload.getContent())) {
            log.warn("유효하지 않은 메시지 페이로드: 사용자={}", user.getEmail());
            return;
        }

        // XSS 방지를 위한 HTML 이스케이프 처리
        String sanitizedContent = HtmlUtils.htmlEscape(payload.getContent().trim());
        
        // 메시지 길이 제한 (추가 검증)
        if (sanitizedContent.length() > 1000) {
            log.warn("메시지 길이 초과: 사용자={}, 길이={}", user.getEmail(), sanitizedContent.length());
            return;
        }

        try {
            // 메시지 저장 및 브로드캐스트
            chatService.saveAndBroadcastMessage(
                payload.getRoomId(),
                user.getEmail(),
                sanitizedContent
            );
            
            log.debug("메시지 전송 성공: 사용자={}, 채팅방={}", user.getEmail(), payload.getRoomId());
        } catch (Exception e) {
            log.error("메시지 전송 실패: 사용자={}, 채팅방={}, 오류={}", 
                user.getEmail(), payload.getRoomId(), e.getMessage(), e);
        }
    }
}
