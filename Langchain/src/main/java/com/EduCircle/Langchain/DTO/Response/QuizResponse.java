package com.EduCircle.Langchain.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String title;
    private Long noteId;
    private String questions;
    private Integer score;
    private Integer maxScore;
    private String quizType;
    private LocalDateTime attemptedAt;
    private LocalDateTime createdAt;
}