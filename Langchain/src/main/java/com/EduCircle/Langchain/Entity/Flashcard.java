package com.EduCircle.Langchain.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "flashcards", indexes = {@Index(name = "idx_flashcards_user_due", columnList = "user_id, next_review_date")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,name = "note_id")
    private Note note;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String question;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String answer;

    @Builder.Default
    private int repetitions=0;

    @Builder.Default
    @Column(name = "ease_factor", precision = 4, scale = 2)
    private BigDecimal easeFactor = new BigDecimal("2.5");

    @Builder.Default
    @Column(name = "next_review_date")
    private LocalDate nextReviewDate=LocalDate.now();

    @CreatedDate
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;


    @Builder.Default
    @Column(name = "interval_days")
    private int intervalDays = 1;



}
