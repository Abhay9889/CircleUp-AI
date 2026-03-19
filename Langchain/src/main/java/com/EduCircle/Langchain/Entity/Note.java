package com.EduCircle.Langchain.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes",indexes = {@Index(name = "idx_notes_user_id",columnList = "user_id"),@Index(name = "idx_notes_status",columnList = "user_id, processing_status")})
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false,length = 500)
    private String title;

    @Column(name = "file_key",length = 500)
    private String fileKey;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "vector_index_id",length = 100)
    private String vectorIndexId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT[]")
    private String[] tags;

    @Column(length = 50)
    @Builder.Default
    private String language="english";

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status",length = 50)
    private ProcessingStatus processingStatus=ProcessingStatus.PENDING;

    @Column(name = "file_type", length = 500)
    private String fileType;

    private LocalDateTime uploadedAt;

    private double difficultyScore;

    public enum ProcessingStatus{
        PENDING, PROCESSING, READY, FAILED
    }
}
