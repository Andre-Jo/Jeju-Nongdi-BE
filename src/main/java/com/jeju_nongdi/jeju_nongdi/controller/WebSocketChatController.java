package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.WebSocketChatMessage;
import com.jeju_nongdi.jeju_nongdi.entity.ChatMessage;
import com.jeju_nongdi.jeju_nongdi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * 채팅 메시지 전송
     */
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/chat/room/{roomId}")
    public WebSocketChatMessage sendMessage(
            @DestinationVariable String roomId,
            @Payload WebSocketChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        log.info("WebSocket message received for room: {} from user: {}", roomId, principal.getName());
        
        try {
            // 현재 시간 설정
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setRoomId(roomId);
            
            // 메시지 타입이 지정되지 않은 경우 CHAT으로 설정
            if (chatMessage.getType() == null) {
                chatMessage.setType(ChatMessage.MessageType.CHAT);
            }
            
            // 발신자 정보가 없는 경우 인증된 사용자로 설정
            if (chatMessage.getSender() == null && principal != null) {
                chatMessage.setSender(principal.getName());
            }
            
            // 실제 메시지를 데이터베이스에 저장 (ChatService 활용)
            if (chatMessage.getType() == ChatMessage.MessageType.CHAT && chatMessage.getContent() != null) {
                try {
                    // WebSocket 메시지를 ChatMessageRequest로 변환하여 저장
                    com.jeju_nongdi.jeju_nongdi.dto.ChatMessageRequest request = 
                        com.jeju_nongdi.jeju_nongdi.dto.ChatMessageRequest.builder()
                            .content(chatMessage.getContent())
                            .build();
                    
                    // Principal을 UserDetails로 변환 (간단한 방법)
                    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        principal.getName(), "", java.util.Collections.emptyList()
                    );
                    
                    // 메시지 저장
                    chatService.sendMessage(roomId, request, userDetails);
                    log.info("Message saved to database for room: {}", roomId);
                    
                } catch (Exception e) {
                    log.warn("Failed to save WebSocket message to database: {}", e.getMessage());
                    // 저장 실패해도 실시간 전송은 계속 진행
                }
            }
            
            log.info("Broadcasting message to room: {}", roomId);
            return chatMessage;
            
        } catch (Exception e) {
            log.error("Error processing WebSocket message for room {}: {}", roomId, e.getMessage());
            
            // 오류 메시지 반환
            return WebSocketChatMessage.builder()
                    .roomId(roomId)
                    .sender("시스템")
                    .content("메시지 전송에 실패했습니다.")
                    .type(ChatMessage.MessageType.SYSTEM)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 사용자 채팅방 입장
     */
    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/chat/room/{roomId}")
    public WebSocketChatMessage addUser(
            @DestinationVariable String roomId,
            @Payload WebSocketChatMessage chatMessage,
            Principal principal) {
        
        log.info("User joining room via WebSocket: {} - User: {}", roomId, principal.getName());
        
        try {
            String username = principal != null ? principal.getName() : chatMessage.getSender();
            
            // ChatService를 통한 입장 처리
            try {
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    username, "", java.util.Collections.emptyList()
                );
                chatService.enterChatRoom(roomId, userDetails);
                log.info("User entered room via ChatService: {}", roomId);
            } catch (Exception e) {
                log.warn("Failed to process room entry via ChatService: {}", e.getMessage());
            }
            
            return WebSocketChatMessage.builder()
                    .roomId(roomId)
                    .sender(username)
                    .content(username + "님이 입장하셨습니다.")
                    .type(ChatMessage.MessageType.ENTER)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing user join for room {}: {}", roomId, e.getMessage());
            return WebSocketChatMessage.createEnterMessage(roomId, "사용자");
        }
    }

    /**
     * 사용자 채팅방 퇴장
     */
    @MessageMapping("/chat.removeUser/{roomId}")
    @SendTo("/topic/chat/room/{roomId}")
    public WebSocketChatMessage removeUser(
            @DestinationVariable String roomId,
            @Payload WebSocketChatMessage chatMessage,
            Principal principal) {
        
        log.info("User leaving room via WebSocket: {} - User: {}", roomId, principal.getName());
        
        try {
            String username = principal != null ? principal.getName() : chatMessage.getSender();
            
            // ChatService를 통한 퇴장 처리
            try {
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    username, "", java.util.Collections.emptyList()
                );
                chatService.leaveChatRoom(roomId, userDetails);
                log.info("User left room via ChatService: {}", roomId);
            } catch (Exception e) {
                log.warn("Failed to process room leave via ChatService: {}", e.getMessage());
            }
            
            return WebSocketChatMessage.builder()
                    .roomId(roomId)
                    .sender(username)
                    .content(username + "님이 퇴장하셨습니다.")
                    .type(ChatMessage.MessageType.LEAVE)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing user leave for room {}: {}", roomId, e.getMessage());
            return WebSocketChatMessage.createLeaveMessage(roomId, "사용자");
        }
    }

    /**
     * 채팅방별 타이핑 상태 전송
     */
    @MessageMapping("/chat.typing/{roomId}")
    @SendTo("/topic/chat/room/{roomId}/typing")
    public WebSocketChatMessage handleTyping(
            @DestinationVariable String roomId,
            @Payload WebSocketChatMessage chatMessage,
            Principal principal) {
        
        try {
            String username = principal != null ? principal.getName() : chatMessage.getSender();
            
            return WebSocketChatMessage.builder()
                    .roomId(roomId)
                    .sender(username)
                    .content("typing")
                    .type(ChatMessage.MessageType.SYSTEM)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing typing status for room {}: {}", roomId, e.getMessage());
            return null;
        }
    }
}
