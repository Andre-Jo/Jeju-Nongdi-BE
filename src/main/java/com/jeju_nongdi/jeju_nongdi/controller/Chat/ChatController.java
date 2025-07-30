package com.jeju_nongdi.jeju_nongdi.controller.Chat;

import com.jeju_nongdi.jeju_nongdi.dto.Chat.ChatRoomDto;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.ChatRoomView;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.MessageDto;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "로그인한 사용자의 모든 채팅방 조회")
    @ApiResponse(responseCode = "200", description = "채팅방 목록 반환")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomView>> getUserChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ChatRoomView> rooms = chatService.getUserChatRooms(user.getEmail());
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "두 사용자 간 채팅방 조회 또는 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 정보 반환"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/room")
    public ResponseEntity<ChatRoomDto> getOrCreateRoomForUsers(
            @Parameter(description = "대화 상대 이메일", required = true)
            @RequestParam String targetEmail,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ChatRoomDto roomDto = chatService.findOrCreate1to1ChatRoom(user.getEmail(), targetEmail);
        return ResponseEntity.ok(roomDto);
    }

    @Operation(summary = "채팅방 내 메시지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 목록 반환"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageDto>> getRoomMessages(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MessageDto> messages = chatService.getMessagesByRoom(roomId, user.getEmail());
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "채팅방 소프트 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (콘텐츠 없음)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> softDeleteRoom(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        chatService.softDeleteRoom(roomId, user.getEmail());
        return ResponseEntity.noContent().build();
    }

}