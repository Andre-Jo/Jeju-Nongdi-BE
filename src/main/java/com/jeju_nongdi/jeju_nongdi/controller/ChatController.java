package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.*;
import com.jeju_nongdi.jeju_nongdi.entity.ChatRoom;
import com.jeju_nongdi.jeju_nongdi.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "채팅", description = "채팅 관련 API")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    @Operation(summary = "채팅방 생성 또는 조회", description = "새로운 채팅방을 생성하거나 기존 채팅방을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "채팅방 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            @Valid @RequestBody @Parameter(description = "채팅방 생성 요청") ChatRoomCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating chat room: type={}, referenceId={}", request.getChatType(), request.getReferenceId());
        ChatRoomResponse response = chatService.createOrGetChatRoom(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/rooms")
    @Operation(summary = "내 채팅방 목록 조회", description = "현재 사용자가 참여 중인 채팅방 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ChatRoomListResponse> getChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching chat rooms for user");
        ChatRoomListResponse response = chatService.getChatRooms(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "채팅 메시지 목록 조회", description = "특정 채팅방의 메시지 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatMessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "채팅방 접근 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Page<ChatMessageResponse>> getChatMessages(
            @Parameter(description = "채팅방 ID") @PathVariable String roomId,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 50) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching messages for room: {}", roomId);
        Page<ChatMessageResponse> response = chatService.getChatMessages(roomId, pageable, userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rooms/{roomId}/messages")
    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = ChatMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "채팅방 접근 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Parameter(description = "채팅방 ID") @PathVariable String roomId,
            @Valid @RequestBody @Parameter(description = "메시지 전송 요청") ChatMessageRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Sending message to room: {}", roomId);
        ChatMessageResponse response = chatService.sendMessage(roomId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/rooms/{roomId}/messages/file")
    @Operation(summary = "파일 메시지 전송", description = "채팅방에 파일과 함께 메시지를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "파일 메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = ChatMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "파일 업로드 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "채팅방 접근 권한 없음", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ChatMessageResponse> sendMessageWithFile(
            @Parameter(description = "채팅방 ID") @PathVariable String roomId,
            @Parameter(description = "첨부 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "메시지 내용") @RequestParam(required = false) String content,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Sending file message to room: {}", roomId);
        ChatMessageRequest request = ChatMessageRequest.builder()
                .content(content)
                .build();
        
        ChatMessageResponse response = chatService.sendMessageWithFile(roomId, request, file, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/rooms/{roomId}/read")
    @Operation(summary = "메시지 읽음 처리", description = "채팅방의 메시지를 읽음 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 읽음 처리 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "채팅방 접근 권한 없음", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void>> markMessagesAsRead(
            @Parameter(description = "채팅방 ID") @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Marking messages as read for room: {}", roomId);
        chatService.markMessagesAsRead(roomId, userDetails);
        return ResponseEntity.ok(com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.success("메시지가 읽음 처리되었습니다.", null));
    }

    @PostMapping("/rooms/{roomId}/enter")
    @Operation(summary = "채팅방 입장", description = "채팅방에 입장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 입장 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "채팅방 접근 권한 없음", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void>> enterChatRoom(
            @Parameter(description = "채팅방 ID") @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("User entering room: {}", roomId);
        chatService.enterChatRoom(roomId, userDetails);
        return ResponseEntity.ok(com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.success("채팅방에 입장했습니다.", null));
    }

    @PostMapping("/rooms/{roomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나갑니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 나가기 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "채팅방 접근 권한 없음", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void>> leaveChatRoom(
            @Parameter(description = "채팅방 ID") @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("User leaving room: {}", roomId);
        chatService.leaveChatRoom(roomId, userDetails);
        return ResponseEntity.ok(com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.success("채팅방에서 나갔습니다.", null));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "읽지 않은 메시지 총 개수", description = "현재 사용자의 읽지 않은 메시지 총 개수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽지 않은 메시지 개수 조회 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Long> getUnreadMessageCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting unread message count");
        Long count = chatService.getUnreadMessageCount(userDetails);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/rooms/search")
    @Operation(summary = "채팅방 검색", description = "키워드로 채팅방을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 검색 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<ChatRoomResponse>> searchChatRooms(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Searching chat rooms with keyword: {}", keyword);
        List<ChatRoomResponse> response = chatService.searchChatRooms(keyword, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms/types/{chatType}")
    @Operation(summary = "타입별 채팅방 조회", description = "특정 타입의 채팅방들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "타입별 채팅방 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<ChatRoomResponse>> getChatRoomsByType(
            @Parameter(description = "채팅방 타입") @PathVariable ChatRoom.ChatType chatType,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting chat rooms by type: {}", chatType);
        List<ChatRoomResponse> response = chatService.getChatRoomsByType(chatType, userDetails);
        return ResponseEntity.ok(response);
    }
}
