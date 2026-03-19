package com.EduCircle.Langchain.Scheduler;

import com.EduCircle.Langchain.Entity.Flashcard;
import com.EduCircle.Langchain.Entity.Notification;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Repository.Flashcardrepository;
import com.EduCircle.Langchain.Repository.Notificationrepository;
import com.EduCircle.Langchain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardReminderJob {

    private final Flashcardrepository flashcardRepository;
    private final UserRepository userRepository;
    private final Notificationrepository notificationRepository;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendFlashcardReminders() {
        log.info("Running flashcard reminder job...");

        List<User> activeUsers = userRepository.findByIsActiveTrue();

        for (User user : activeUsers) {
            long dueCount = flashcardRepository
                    .countByUserIdAndNextReviewDateLessThanEqual(user.getId(), LocalDate.now());

            if (dueCount > 0) {
                Notification notification = Notification.builder()
                        .user(user)
                        .type(Notification.NotificationType.FLASHCARD_DUE)
                        .title("Flashcards due for review!")
                        .message("You have " + dueCount + " flashcard(s) due for review today.")
                        .build();

                notificationRepository.save(notification);
                log.debug("Reminder created for user {} - {} cards due", user.getEmail(), dueCount);
            }
        }

        log.info("Flashcard reminder job complete.");
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkStreaks() {
        log.info("Checking study streaks...");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<User> usersWhoMissed = userRepository.findByLastActiveBefore(yesterday);
        for (User user : usersWhoMissed) {
            if (user.getStudyStreak() > 0) {
                user.setStudyStreak(0);
                userRepository.save(user);

                Notification notification = Notification.builder()
                        .user(user)
                        .type(Notification.NotificationType.STREAK_ALERT)
                        .title("Streak lost!")
                        .message("Your study streak was reset. Come back today to start a new one!")
                        .build();
                notificationRepository.save(notification);
            }
        }
        log.info("Streak check complete.");
    }
}