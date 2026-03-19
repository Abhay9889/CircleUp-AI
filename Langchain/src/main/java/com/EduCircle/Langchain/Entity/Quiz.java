package com.EduCircle.Langchain.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "JSONB",nullable = false)
    private String questions;

    private Integer score;
    private Integer maxScore;
    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type",length = 20)
    @Builder.Default
    private QuizType quizType=QuizType.MCQ;

    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime attemptedAt;


    public enum QuizType {
        MCQ, TRUE_FALSE, SHORT_ANSWER, MIXED
    }
}
