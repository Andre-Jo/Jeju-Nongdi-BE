package com.jeju_nongdi.jeju_nongdi.repository.Chat;

import com.jeju_nongdi.jeju_nongdi.entity.Chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 메시지 Repository
 * 
 * 채팅 메시지의 데이터베이스 접근을 담당합니다.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 채팅방의 모든 메시지를 시간순으로 조회
     * 
     * @param roomId 채팅방 ID
     * @return 메시지 목록 (오래된 순)
     */
    List<Message> findByRoomIdOrderByCreatedAtAsc(String roomId);
    
    /**
     * 채팅방의 메시지를 페이징하여 조회 (최신 메시지부터)
     * 
     * @param roomId 채팅방 ID
     * @param pageable 페이징 정보
     * @return 페이징된 메시지 목록
     */
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId ORDER BY m.createdAt DESC")
    Page<Message> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") String roomId, Pageable pageable);
    
    /**
     * 채팅방의 가장 최근 메시지 1개 조회
     * 마지막 메시지 미리보기에 사용
     * 
     * @param roomId 채팅방 ID
     * @return 최근 메시지 (Optional)
     */
    Optional<Message> findTopByRoomIdOrderByCreatedAtDesc(String roomId);
    
    /**
     * 특정 채팅방의 모든 메시지 삭제
     * 
     * @param roomId 채팅방 ID
     */
    @Transactional
    void deleteAllByRoomId(String roomId);
    
    /**
     * 특정 접두사로 시작하는 채팅방들의 메시지를 시간순으로 조회
     * 
     * @param baseRoomId 채팅방 ID 접두사
     * @return 메시지 목록 (오래된 순)
     */
    List<Message> findByRoomIdStartingWithOrderByCreatedAtAsc(String baseRoomId);
    
    /**
     * 채팅방의 메시지 개수 조회
     * 
     * @param roomId 채팅방 ID
     * @return 메시지 개수
     */
    long countByRoomId(String roomId);
    
    /**
     * 특정 사용자가 보낸 메시지 개수 조회
     * 
     * @param senderId 발신자 ID
     * @return 메시지 개수
     */
    long countBySenderId(Long senderId);
    
    /**
     * 특정 시간 이후의 채팅방 메시지들 조회
     * 
     * @param roomId 채팅방 ID
     * @param timestamp 기준 시간
     * @return 메시지 목록
     */
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.createdAt > :timestamp ORDER BY m.createdAt ASC")
    List<Message> findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
        @Param("roomId") String roomId, 
        @Param("timestamp") java.time.LocalDateTime timestamp
    );
}
