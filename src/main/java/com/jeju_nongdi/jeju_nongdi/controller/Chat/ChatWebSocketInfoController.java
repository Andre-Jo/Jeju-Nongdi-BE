package com.jeju_nongdi.jeju_nongdi.controller.Chat;

import com.jeju_nongdi.jeju_nongdi.dto.ApiResponse;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.WebSocketConnectionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket 연결 정보 제공 컨트롤러
 * 
 * 실제 WebSocket은 HTTP 엔드포인트가 아니므로 Swagger에서 직접 문서화할 수 없습니다.
 * 이 컨트롤러는 WebSocket 연결 방법과 사용법을 문서화하기 위한 정보를 제공합니다.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat WebSocket", description = "실시간 채팅 WebSocket 연결 정보")
public class ChatWebSocketInfoController {

    @Operation(
        summary = "WebSocket 연결 정보 조회",
        description = """
            실시간 채팅을 위한 WebSocket 연결 정보를 제공합니다.
            
            ## WebSocket 연결 방법:
            
            ### 1. 연결 설정
            ```javascript
            BASE_URL(http://localhost:8080)/api/ws
            
            - 응답 코드 -
            200 : 연결 성공(Ok)
            400 : 잘못된 요청(Bad Request)
            ```
            
            ### 2. 연결 및 인증
            ```javascript
            BASE_URL(http://localhost:8080)/api/ws/topic/chat/room/{roomId}
            
           *실시간으로 해당 채팅방의 메시지를 수신함
        
           - 응답 코드 -
           200 : 구독 연결 성공(Ok)
           400 : 잘못된 요청(Bad Request)
            ```
            
            ### 3. 메시지 전송
            ```javascript
            BASE_URL(http://localhost:8080)/api/ws
             Destination: /app/chat.sendPrivateMessage
             Method: STOMP SEND
        
             RequestBody
             {
               String roomId,
               String content
             }
        
             - 응답 코드 -
             200 : 발행 연결 및 전송 성공(Ok)
             {
               Long id,
               String roomId,
               Long senderId,
               Long receiverId,
               String content,
               LocalDateTime createdAt
             }
        
             400 : 잘못된 요청(Bad Request)
            ```
            
            ### 4. 연결 해제
            ```javascript
            stompClient.disconnect(function() {
                console.log('Disconnected');
            });
            ```
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "WebSocket 연결 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = WebSocketConnectionInfo.class))
        )
    })
    @GetMapping("/websocket-info")
    public ResponseEntity<ApiResponse<WebSocketConnectionInfo>> getWebSocketInfo() {
        WebSocketConnectionInfo info = WebSocketConnectionInfo.builder()
                .endpoint("/api/ws")
                .sendDestination("/app/chat.sendPrivateMessage")
                .subscribePattern("/topic/chat/room/{roomId}")
                .protocol("STOMP over WebSocket")
                .sockJsEnabled(true)
                .authentication("JWT Bearer Token in WebSocket headers")
                .build();

        return ResponseEntity.ok(
            ApiResponse.success( "WebSocket 연결 정보 조회 성공", info)
        );
    }
}
