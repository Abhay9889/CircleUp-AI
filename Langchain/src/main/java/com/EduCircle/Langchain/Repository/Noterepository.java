package com.EduCircle.Langchain.Repository;

import com.EduCircle.Langchain.Entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface Noterepository extends JpaRepository<Note,Long> {
    List<Note> findByUserIdOrderByUploadedAtDesc(Long userId);

    List<Note> findByUserIdAndProcessingStatusOrderByUploadedAtDesc(
            Long userId,
            Note.ProcessingStatus processingStatus
    );

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);


    @Query(value = "SELECT * FROM notes WHERE user_id = :userId AND :tag = ANY(tags)",
            nativeQuery = true)
    List<Note> findByUserIdAndTag(@Param("userId") Long userId, @Param("tag") String tag);

    void deleteByIdAndUserId(Long id, Long userId);
}
