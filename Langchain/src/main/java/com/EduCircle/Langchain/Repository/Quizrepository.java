package com.EduCircle.Langchain.Repository;

import com.EduCircle.Langchain.Entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface Quizrepository extends JpaRepository<Quiz,Long> {
    List<Quiz> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Quiz> findByUserIdAndNoteId(Long userId, Long noteId);
    Optional<Quiz> findByIdAndUserId(Long id, Long userId);
}
