package com.EduCircle.Langchain.Service;

import com.EduCircle.Langchain.Entity.Note;
import com.EduCircle.Langchain.Entity.Quiz;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.Noterepository;
import com.EduCircle.Langchain.Repository.Quizrepository;
import com.EduCircle.Langchain.Repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final Quizrepository quizRepository;
    private final Noterepository noteRepository;
    private final UserRepository userRepository;

    @Qualifier("aiWebClient")
    private final WebClient aiWebClient;

    private final ObjectMapper objectMapper;


    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateQuiz(
            Long noteId,
            int count,
            String type,
            String userEmail) {

        User user = getUser(userEmail);

        Note note = noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        Map<String, Object> aiResponse = aiWebClient.post()
                .uri("/quiz/generate")
                .bodyValue(Map.of(
                        "note_id", noteId,
                        "count", count,
                        "type", type.toLowerCase()
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        if (aiResponse == null) {
            throw new RuntimeException("AI service returned null");
        }

        try {
            String questionsJson =
                    objectMapper.writeValueAsString(aiResponse.get("questions"));

            Quiz quiz = Quiz.builder()
                    .user(user)
                    .note(note)
                    .title("Quiz: " + note.getTitle())
                    .questions(questionsJson)
                    .maxScore(count)
                    .quizType(Quiz.QuizType.valueOf(type.toUpperCase()))
                    .build();

            Quiz saved = quizRepository.save(quiz);

            return Map.of(
                    "quizId", saved.getId(),
                    "title", saved.getTitle(),
                    "questions", aiResponse.get("questions"),
                    "maxScore", count
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to save quiz: " + e.getMessage());
        }
    }


    @Transactional
    public Map<String, Object> submitQuiz(
            Long quizId,
            Map<String, Object> answers,
            String userEmail) {

        User user = getUser(userEmail);

        Quiz quiz = quizRepository.findByIdAndUserId(quizId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        Map<String, Object> result = aiWebClient.post()
                .uri("/quiz/evaluate")
                .bodyValue(Map.of(
                        "quiz_id", quizId,
                        "questions", quiz.getQuestions(),
                        "answers", answers
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        int score = result != null
                ? ((Number) result.getOrDefault("score", 0)).intValue()
                : 0;

        quiz.setScore(score);
        quiz.setAttemptedAt(LocalDateTime.now());
        quizRepository.save(quiz);

        return Map.of(
                "quizId", quizId,
                "score", score,
                "maxScore", quiz.getMaxScore(),
                "percent", quiz.getMaxScore() > 0
                        ? (score * 100 / quiz.getMaxScore())
                        : 0,
                "feedback", result != null
                        ? result.getOrDefault("feedback", List.of())
                        : List.of()
        );
    }


    public List<Map<String, Object>> getUserQuizzes(String userEmail) {

        User user = getUser(userEmail);

        return quizRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(q -> Map.<String, Object>of(
                        "id", q.getId(),
                        "title", q.getTitle() != null ? q.getTitle() : "",
                        "quizType", q.getQuizType(),
                        "score", q.getScore() != null ? q.getScore() : "-",
                        "maxScore", q.getMaxScore() != null ? q.getMaxScore() : 0,
                        "attemptedAt", q.getAttemptedAt() != null
                                ? q.getAttemptedAt().toString()
                                : null,
                        "createdAt", q.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
    }


    public Map<String, Object> getQuiz(Long quizId, String userEmail) {

        User user = getUser(userEmail);

        Quiz quiz = quizRepository.findByIdAndUserId(quizId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        return Map.of(
                "id", quiz.getId(),
                "title", quiz.getTitle() != null ? quiz.getTitle() : "",
                "questions", quiz.getQuestions(),
                "quizType", quiz.getQuizType(),
                "score", quiz.getScore() != null ? quiz.getScore() : "-",
                "maxScore", quiz.getMaxScore() != null ? quiz.getMaxScore() : 0
        );
    }


    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}