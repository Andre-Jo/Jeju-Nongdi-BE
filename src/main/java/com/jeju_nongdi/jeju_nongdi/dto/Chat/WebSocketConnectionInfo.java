package com.jeju_nongdi.jeju_nongdi.dto.Chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 연결 정보 DTO
 * 
 * Swagger 문서화를 위한 WebSocket 연결 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WebSocket 연결 정보")
public class WebSocketConnectionInfo {
    
    @Schema(description = "WebSocket 연결 엔드포인트", example = "/api/ws")
    private String endpoint;
    
    @Schema(description = "메시지 전송 주소", example = "/app/chat.sendPrivateMessage")
    private String sendDestination;
    
    @Schema(description = "채팅방 구독 주소 패턴", example = "/topic/chat/room/{roomId}")
    private String subscribePattern;
    
    @Schema(description = "연결 프로토콜", example = "STOMP over WebSocket")
    private String protocol;
    
    @Schema(description = "SockJS 지원 여부", example = "true")
    private boolean sockJsEnabled;
    
    @Schema(description = "인증 방법", example = "JWT Bearer Token in WebSocket headers")
    private String authentication;
}
