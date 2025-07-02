package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.AuthResponse;
import com.jeju_nongdi.jeju_nongdi.dto.LoginRequest;
import com.jeju_nongdi.jeju_nongdi.dto.SignupRequest;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.exception.EmailAlreadyExistsException;
import com.jeju_nongdi.jeju_nongdi.exception.NicknameAlreadyExistsException;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import com.jeju_nongdi.jeju_nongdi.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;
    private final String TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        signupRequest =
                new SignupRequest("integration@test.com", "password123", "Integration Test", "integrationuser", "01012345678" );

        loginRequest = new LoginRequest("integration@test.com","password123");

        user = User.builder()
                .id(1L)
                .email("integration@test.com")
                .password("encodedPassword")
                .name("Integration Test")
                .nickname("integrationuser")
                .phone("01012345678")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.existsByNickname(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.generateToken(anyString())).willReturn(TOKEN);

        // when
        AuthResponse response = userService.signup(signupRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(TOKEN);
        assertThat(response.email()).isEqualTo("integration@test.com");
        assertThat(response.nickname()).isEqualTo("integrationuser");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(anyString());
    }

    @Test
    @DisplayName("이메일 중복으로 회원가입 실패 테스트")
    void signupFailDueToEmailDuplicate() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.signup(signupRequest);
        });
    }

    @Test
    @DisplayName("닉네임 중복으로 회원가입 실패 테스트")
    void signupFailDueToNicknameDuplicate() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.existsByNickname(anyString())).willReturn(true);

        // when & then
        assertThrows(NicknameAlreadyExistsException.class, () -> {
            userService.signup(signupRequest);
        });
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(user);
        given(jwtUtil.generateToken(anyString())).willReturn(TOKEN);

        // when
        AuthResponse response = userService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(TOKEN);
        assertThat(response.email()).isEqualTo("integration@test.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(anyString());
    }

    @Test
    @DisplayName("현재 사용자 조회 테스트")
    void getCurrentUser() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        // when
        User foundUser = userService.getCurrentUser("integration@test.com");

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("integration@test.com");
        verify(userRepository).findByEmail(anyString());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생 테스트")
    void getCurrentUserNotFound() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            userService.getCurrentUser("nonexistent@example.com");
        });
    }
}
