package com.jeju_nongdi.jeju_nongdi.repository.Chat;

import com.jeju_nongdi.jeju_nongdi.entity.Chat.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomIdOrderByCreatedAtAsc(String roomId);
    // 특정 채팅방의 가장 최근 메시지 1개를 조회 (마지막 메시지 미리보기에 사용)
    Optional<Message> findTopByRoomIdOrderByCreatedAtDesc(String roomId);
    @Transactional
    void deleteAllByRoomId(String roomId);
    List<Message> findByRoomIdStartingWithOrderByCreatedAtAsc(String baseRoomId);
}