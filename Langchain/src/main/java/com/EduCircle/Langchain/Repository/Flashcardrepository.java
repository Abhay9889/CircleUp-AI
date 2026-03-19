package com.EduCircle.Langchain.Repository;

import com.EduCircle.Langchain.Entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Flashcardrepository extends JpaRepository<Flashcard,Long> {
    List<Flashcard> findByUserIdOrderByNextReviewDateAsc(Long userId);


    List<Flashcard> findByUserIdAndNextReviewDateLessThanEqual(Long userId, LocalDate date);

    long countByUserIdAndNextReviewDateLessThanEqual(Long userId, LocalDate date);

    List<Flashcard> findByUserIdAndNoteId(Long userId, Long noteId);

    Optional<Flashcard> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
