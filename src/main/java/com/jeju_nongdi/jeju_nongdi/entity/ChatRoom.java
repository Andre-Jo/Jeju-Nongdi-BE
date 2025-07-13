package com.jeju_nongdi.jeju_nongdi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId; // UUID 형태

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type", nullable = false)
    private ChatType chatType;

    @Column(name = "reference_id")
    private Long referenceId; // 멘토링 ID 또는 농지 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // 채팅방 생성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant; // 참여자

    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 채팅방 타입 enum
    public enum ChatType {
        MENTORING("멘토링"),
        FARMLAND("농지문의"),
        JOB_POSTING("일자리문의"),
        GENERAL("일반채팅");

        private final String koreanName;

        ChatType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }

    // 비즈니스 메서드
    public boolean isParticipant(User user) {
        return creator.equals(user) || participant.equals(user);
    }

    public User getOtherParticipant(User currentUser) {
        if (creator.equals(currentUser)) {
            return participant;
        } else if (participant.equals(currentUser)) {
            return creator;
        }
        return null;
    }

    public void updateLastMessage(String message) {
        this.lastMessage = message;
        this.lastMessageTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
