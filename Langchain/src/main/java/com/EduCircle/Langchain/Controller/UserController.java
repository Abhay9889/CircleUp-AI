package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        return ResponseEntity.ok(toProfile(user));
    }

    @PatchMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());

        if (req.containsKey("name") && !req.get("name").isBlank()) {
            user.setName(req.get("name").trim());
        }
        if (req.containsKey("preferredLanguage")) {
            user.setPreferredLanguage(req.get("preferredLanguage"));
        }
        userRepository.save(user);
        return ResponseEntity.ok(toProfile(user));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Map<String, Object> toProfile(User user) {
        return Map.of(
                "id",                user.getId(),
                "name",              user.getName(),
                "email",             user.getEmail(),
                "role",              user.getRole().name(),
                "preferredLanguage", user.getPreferredLanguage(),
                "studyStreak",       user.getStudyStreak(),
                "lastActive",        user.getLastActive() != null ? user.getLastActive().toString() : "",
                "createdAt",         user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
        );
    }
}