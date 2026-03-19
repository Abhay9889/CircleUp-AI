package com.EduCircle.Langchain.Service;

import com.EduCircle.Langchain.DTO.Request.LoginRequest;
import com.EduCircle.Langchain.DTO.Response.AuthResponse;
import com.EduCircle.Langchain.DTO.Request.RegisterRequest;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.EmailAlreadyExistsException;
import com.EduCircle.Langchain.Exception.InvalidTokenException;
import com.EduCircle.Langchain.Repository.UserRepository;
import com.EduCircle.Langchain.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
//import com.EduCircle.Langchain.DTO.Request.RestoreRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;


    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already registered: " + req.getEmail()
            );
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.STUDENT)
                .build();

        userRepository.save(user);
        log.info("Registered new user: {}", user.getEmail());

        return buildTokenPair(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastActive(LocalDate.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());
        return buildTokenPair(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidTokenException("Not a refresh token");
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildTokenPair(user);
    }

    public void logout(String accessToken) {
        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "1",
                Duration.ofMillis(jwtTokenProvider.getExpiryMs())
        );
        log.debug("Token blacklisted on logout");
    }

    public Map<String, Object> getCurrentUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole(),
                "preferredLanguage", user.getPreferredLanguage(),
                "studyStreak", user.getStudyStreak(),
                "createdAt", user.getCreatedAt()
        );
    }

    private AuthResponse buildTokenPair(User user) {
        String accessToken  = jwtTokenProvider.generateAccessToken(
                user.getEmail(), user.getId(), user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}