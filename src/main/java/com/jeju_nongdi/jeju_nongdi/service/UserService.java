package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.*;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.exception.EmailAlreadyExistsException;
import com.jeju_nongdi.jeju_nongdi.exception.NicknameAlreadyExistsException;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import com.jeju_nongdi.jeju_nongdi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse signup(SignupRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(request.nickname())) {
            throw new NicknameAlreadyExistsException("이미 사용 중인 닉네임입니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .nickname(request.nickname())
                .profileImage(request.profileImage())
                .phone(request.phone())
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getNickname(), user.getRole().name());

    }

    public AuthResponse login(LoginRequest request) {
        // 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = (User) authentication.getPrincipal();

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getNickname(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public CheckNicknameResponse checkNicknameAvailability(CheckNicknameRequest request) {
        boolean isAvailable = !userRepository.existsByNickname(request.nickname());
        String message = isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return new CheckNicknameResponse(isAvailable, message);
    }

    // 닉네임 변경
    public User updateNickname(String userEmail, UpdateNicknameRequest request) {
        User user = getCurrentUser(userEmail);

        // 현재 닉네임과 동일한지 확인
        if (user.getNickname().equals(request.nickname())) {
            throw new IllegalArgumentException("현재 닉네임과 동일합니다.");
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.nickname())) {
            throw new NicknameAlreadyExistsException("이미 사용 중인 닉네임입니다.");
        }

        user.setNickname(request.nickname());
        return userRepository.save(user);
    }

    // 비밀번호 변경
    public void updatePassword(String userEmail, UpdatePasswordRequest request) {
        User user = getCurrentUser(userEmail);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // 프로필 이미지 변경
    public User updateProfileImage(String userEmail, UpdateProfileImageRequest request) {
        User user = getCurrentUser(userEmail);
        user.setProfileImage(request.profileImage());
        return userRepository.save(user);
    }
}
