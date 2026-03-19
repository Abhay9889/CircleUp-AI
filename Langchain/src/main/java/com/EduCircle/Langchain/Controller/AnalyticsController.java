package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getDashboard(userDetails.getUsername()));
    }

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeekly(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getDashboard(userDetails.getUsername()));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<?> getHeatmap(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getHeatmap(userDetails.getUsername()));
    }

    @GetMapping("/streak")
    public ResponseEntity<?> getStreak(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getStreak(userDetails.getUsername()));
    }
}