package com.EduCircle.Langchain.Repository;

import com.EduCircle.Langchain.Entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface Studysessionrepository extends JpaRepository<StudySession,Long> {
    List<StudySession> findByUserIdOrderByStartedAtDesc(Long userId);

    @Query("SELECT s FROM StudySession s WHERE s.user.id = :userId " +
            "AND s.startedAt >= :from AND s.startedAt <= :to")
    List<StudySession> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT COALESCE(SUM(s.durationSec), 0) FROM StudySession s " +
            "WHERE s.user.id = :userId AND s.startedAt >= :from")
    Long sumDurationByUserIdSince(@Param("userId") Long userId, @Param("from") LocalDateTime from);


}
