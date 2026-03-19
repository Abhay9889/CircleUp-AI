package com.EduCircle.Langchain.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "users",indexes = {@Index(name = "idx_users_email",columnList = "email")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false,length = 100)
    private String email;

    @Column(name = "password_hash",nullable = false)
    private String passwordHash;

    @Column(nullable = false,length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    @Builder.Default
    private Role role=Role.STUDENT;

    @Column(name = "preferredLanguage ",length = 30)
    @Builder.Default
    private String preferredLanguage="english";

    @Column(name = "studyStreak")
    @Builder.Default
    private int studyStreak=0;

    private LocalDate lastActive;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive=true;

    @CreatedDate
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    public enum Role{
        STUDENT,TEACHER,ADMIN
    }
}
