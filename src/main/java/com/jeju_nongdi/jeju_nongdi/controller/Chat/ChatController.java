package com.jeju_nongdi.jeju_nongdi.controller.Chat;

import com.jeju_nongdi.jeju_nongdi.dto.ApiResponse;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.ChatRoomDto;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.ChatRoomView;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.MessageDto;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat", description = "채팅 관련 API")
public class ChatController {

    private final ChatService chatService;

    @Operation(
        summary = "사용자 채팅방 목록 조회", 
        description = "로그인한 사용자의 모든 채팅방을 조회합니다. 삭제된 채팅방은 제외됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomView.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomView>>> getUserChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증이 필요합니다", HttpStatus.UNAUTHORIZED.value()));
        }

        try {
            List<ChatRoomView> rooms = chatService.getUserChatRooms(user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("채팅방 목록 조회 성공", rooms));
        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(
        summary = "1:1 채팅방 조회/생성", 
        description = "두 사용자 간의 채팅방을 조회하거나 없으면 새로 생성합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "채팅방 조회/생성 성공",
            content = @Content(schema = @Schema(implementation = ChatRoomDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 (유효하지 않은 이메일 등)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "대상 사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/room")
    public ResponseEntity<ApiResponse<ChatRoomDto>> getOrCreateRoomForUsers(
            @Parameter(description = "대화 상대 이메일", required = true, example = "user@example.com")
            @RequestParam @Email(message = "유효한 이메일 형식이어야 합니다") String targetEmail,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증이 필요합니다", HttpStatus.UNAUTHORIZED.value()));
        }

        if (user.getEmail().equals(targetEmail)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("자기 자신과는 채팅할 수 없습니다", HttpStatus.BAD_REQUEST.value()));
        }

        try {
            ChatRoomDto roomDto = chatService.findOrCreate1to1ChatRoom(user.getEmail(), targetEmail);
            return ResponseEntity.ok(ApiResponse.success("채팅방 조회/생성 성공", roomDto));
        } catch (IllegalArgumentException e) {
            log.warn("채팅방 생성 실패 - 사용자를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("대상 사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            log.error("채팅방 조회/생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(
        summary = "채팅방 메시지 조회 (페이징)", 
        description = "특정 채팅방의 메시지를 페이징하여 조회합니다. 최신 메시지부터 조회됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "메시지 조회 성공 (페이징)",
            content = @Content(schema = @Schema(implementation = MessageDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "채팅방 접근 권한 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "채팅방을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/rooms/{roomId}/messages/paged")
    public ResponseEntity<ApiResponse<Page<MessageDto>>> getRoomMessagesWithPaging(
            @Parameter(description = "채팅방 ID", required = true, example = "1_2")
            @PathVariable String roomId,
            @Parameter(description = "페이징 정보 (page=0부터 시작, size=기본값 20)")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증이 필요합니다", HttpStatus.UNAUTHORIZED.value()));
        }

        try {
            Page<MessageDto> messages = chatService.getMessagesByRoomWithPaging(roomId, user.getEmail(), pageable);
            return ResponseEntity.ok(ApiResponse.success("메시지 페이징 조회 성공", messages));
        } catch (SecurityException e) {
            log.warn("채팅방 접근 권한 없음: 사용자={}, 채팅방={}", user.getEmail(), roomId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("채팅방 접근 권한이 없습니다", HttpStatus.FORBIDDEN.value()));
        } catch (IllegalArgumentException e) {
            log.warn("채팅방을 찾을 수 없음: roomId={}", roomId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("채팅방을 찾을 수 없습니다", HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            log.error("메시지 페이징 조회 실패: roomId={}, error={}", roomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(
        summary = "채팅방 메시지 조회 (전체)",
        description = "특정 채팅방의 모든 메시지를 시간순으로 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "메시지 조회 성공",
            content = @Content(schema = @Schema(implementation = MessageDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "채팅방 접근 권한 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "채팅방을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getRoomMessages(
            @Parameter(description = "채팅방 ID", required = true, example = "1_2")
            @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증이 필요합니다", HttpStatus.UNAUTHORIZED.value()));
        }

        try {
            List<MessageDto> messages = chatService.getMessagesByRoom(roomId, user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("메시지 조회 성공", messages));
        } catch (SecurityException e) {
            log.warn("채팅방 접근 권한 없음: 사용자={}, 채팅방={}", user.getEmail(), roomId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("채팅방 접근 권한이 없습니다", HttpStatus.FORBIDDEN.value()));
        } catch (IllegalArgumentException e) {
            log.warn("채팅방을 찾을 수 없음: roomId={}", roomId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("채팅방을 찾을 수 없습니다", HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            log.error("메시지 조회 실패: roomId={}, error={}", roomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(
        summary = "채팅방 삭제", 
        description = "채팅방을 소프트 삭제합니다. 상대방이 삭제하면 완전 삭제됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "채팅방 삭제 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "채팅방 삭제 권한 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "채팅방을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> softDeleteRoom(
            @Parameter(description = "채팅방 ID", required = true, example = "1_2")
            @PathVariable String roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증이 필요합니다", HttpStatus.UNAUTHORIZED.value()));
        }

        try {
            chatService.softDeleteRoom(roomId, user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("채팅방이 삭제되었습니다", null));
        } catch (SecurityException e) {
            log.warn("채팅방 삭제 권한 없음: 사용자={}, 채팅방={}", user.getEmail(), roomId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("채팅방 삭제 권한이 없습니다", HttpStatus.FORBIDDEN.value()));
        } catch (IllegalArgumentException e) {
            log.warn("채팅방을 찾을 수 없음: roomId={}", roomId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("채팅방을 찾을 수 없습니다", HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            log.error("채팅방 삭제 실패: roomId={}, error={}", roomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
