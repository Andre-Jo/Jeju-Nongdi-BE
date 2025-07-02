package com.jeju_nongdi.jeju_nongdi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("User 엔티티 생성 테스트")
    void createUserEntity() {
        // given
        String email = "test@example.com";
        String password = "encodedPassword";
        String name = "Test User";
        String nickname = "testuser";
        String phone = "01012345678";
        User.Role role = User.Role.USER;

        // when
        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .nickname(nickname)
                .phone(phone)
                .role(role)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getPhone()).isEqualTo(phone);
        assertThat(user.getRole()).isEqualTo(role);
    }
}
