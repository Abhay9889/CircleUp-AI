package com.EduCircle.Langchain.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type",length = 500)
    private SessionType sessionType;

    @Column(name = "duration_sec")
    private Integer durationSec;

    private Integer score;

    @CreatedDate
    @Column(name = "started_at",updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;


    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    public enum SessionType{
        FLASHCARD, QUIZ, READING, ASK, VOICE
    }
}
