package com.jeju_nongdi.jeju_nongdi.entity.Chat;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "chat_rooms")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
    @Id
    @Column(name = "room_id", length = 50)
    private String roomId;

    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "chat_room_deleted_users", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "user_email")
    @Builder.Default
    private Set<String> deletedByUsers = new HashSet<>();

    public static String generateBaseRoomId(Long id1, Long id2) {
        return Stream.of(id1, id2)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining("_"));
    }
}