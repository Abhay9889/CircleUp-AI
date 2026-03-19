package com.EduCircle.Langchain.Scheduler;

import com.EduCircle.Langchain.Entity.Notification;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Repository.Flashcardrepository;
import com.EduCircle.Langchain.Repository.Notificationrepository;
import com.EduCircle.Langchain.Repository.Studysessionrepository;
import com.EduCircle.Langchain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportJob {

    private final UserRepository         userRepository;
    private final Studysessionrepository sessionRepository;
    private final Flashcardrepository    flashcardRepository;
    private final Notificationrepository notificationRepository;

    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyReports() {
        log.info("Generating weekly study reports...");

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<User> users = userRepository.findByIsActiveTrue();

        for (User user : users) {
            try {
                Long studyTimeSec = sessionRepository
                        .sumDurationByUserIdSince(user.getId(), oneWeekAgo);
                long studyMinutes = studyTimeSec != null ? studyTimeSec / 60 : 0;

                long dueCards = flashcardRepository
                        .countByUserIdAndNextReviewDateLessThanEqual(user.getId(), LocalDate.now());

                String message = String.format(
                        "This week: %d mins studied, %d streak days. Cards due: %d.",
                        studyMinutes, user.getStudyStreak(), dueCards
                );

                Notification notification = Notification.builder()
                        .user(user)
                        .type(Notification.NotificationType.WEEKLY_REPORT)
                        .title("Your Weekly Study Report 📊")
                        .message(message)
                        .build();
                notificationRepository.save(notification);

            } catch (Exception e) {
                log.error("Weekly report failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Weekly reports sent to {} users.", users.size());
    }
}
