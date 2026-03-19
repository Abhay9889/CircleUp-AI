package com.EduCircle.Langchain.Service;

import com.EduCircle.Langchain.Entity.StudySession;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.Flashcardrepository;
import com.EduCircle.Langchain.Repository.Noterepository;
import com.EduCircle.Langchain.Repository.Studysessionrepository;
import com.EduCircle.Langchain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final Studysessionrepository sessionRepository;
    private final Flashcardrepository flashcardRepository;
    private final Noterepository noteRepository;

    public Map<String, Object> getDashboard(String userEmail) {
        User user = getUser(userEmail);
        Long userId = user.getId();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        long totalNotes      = noteRepository.countByUserId(userId);
        long dueCards        = flashcardRepository.countByUserIdAndNextReviewDateLessThanEqual(userId, LocalDate.now());
        Long studyTimeSec    = sessionRepository.sumDurationByUserIdSince(userId, thirtyDaysAgo);
        int  streak          = user.getStudyStreak();

        return Map.of(
                "totalNotes",       totalNotes,
                "dueFlashcards",    dueCards,
                "studyTimeMinutes", studyTimeSec != null ? studyTimeSec / 60 : 0,
                "studyStreak",      streak,
                "lastActive",       user.getLastActive() != null ? user.getLastActive().toString() : null
        );
    }

    public Map<String, Object> getHeatmap(String userEmail) {
        User user = getUser(userEmail);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);

        List<StudySession> sessions = sessionRepository
                .findByUserIdAndDateRange(user.getId(), oneYearAgo, LocalDateTime.now());


        Map<String, Long> heatmap = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return Map.of("heatmap", heatmap);
    }

    public Map<String, Object> getStreak(String userEmail) {
        User user = getUser(userEmail);
        return Map.of(
                "currentStreak", user.getStudyStreak(),
                "lastActive",    user.getLastActive() != null ? user.getLastActive().toString() : null
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
