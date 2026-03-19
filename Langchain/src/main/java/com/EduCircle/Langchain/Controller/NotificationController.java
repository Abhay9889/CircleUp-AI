package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Entity.Notification;
import com.EduCircle.Langchain.Repository.Notificationrepository;
import com.EduCircle.Langchain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final Notificationrepository notificationRepository;
    private final UserRepository userRepository;


    @GetMapping
    public ResponseEntity<List<Notification>> getUnread(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
        return ResponseEntity.ok(
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
        );
    }


    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }


    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
        notificationRepository.markAllReadByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}

