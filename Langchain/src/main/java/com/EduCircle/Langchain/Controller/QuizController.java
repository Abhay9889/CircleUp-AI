package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    @PostMapping("/generate/{noteId}")
    public ResponseEntity<Map<String, Object>> generate(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "MCQ") String type,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.generateQuiz(noteId, count, type, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.getUserQuizzes(userDetails.getUsername()));
    }


    @PostMapping("/{quizId}/submit")
    public ResponseEntity<Map<String, Object>> submit(
            @PathVariable Long quizId,
            @RequestBody Map<String, Object> answers,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.submitQuiz(quizId, answers, userDetails.getUsername()));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<Map<String, Object>> get(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.getQuiz(quizId, userDetails.getUsername()));
    }
}

