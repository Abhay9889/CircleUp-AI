package com.EduCircle.Langchain.Scheduler;

import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportScheduler {

    private final UserRepository         userRepository;
    private final Studysessionrepository studySessionRepository;
    private final Flashcardrepository    flashcardRepository;
    private final Noterepository         noteRepository;
    private final JavaMailSender         mailSender;

    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyReports() {
        log.info("Sending weekly study reports...");

        List<User> users = userRepository.findByIsActiveTrue();
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        for (User user : users) {
            try {
                sendReport(user, weekAgo);
            } catch (Exception e) {
                log.error("Failed to send weekly report to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Weekly reports sent to {} users", users.size());
    }

    private void sendReport(User user, LocalDateTime weekAgo) {
        long sessions = studySessionRepository
                .findByUserIdAndDateRange(user.getId(), weekAgo, LocalDateTime.now())
                .size();

        long dueCards = flashcardRepository
                .countByUserIdAndNextReviewDateLessThanEqual(user.getId(), LocalDate.now());

        Long studySecs = studySessionRepository
                .sumDurationByUserIdSince(user.getId(), weekAgo);
        long studyMin = studySecs != null ? studySecs / 60 : 0;

        long totalNotes = noteRepository.countByUserId(user.getId());

        String body = String.format("""
            Hi %s,
            
            Here's your EduCircle weekly summary:
            
             Study sessions this week:  %d
             Total study time:           %d minutes
             Total notes:               %d
             Flashcards due today:      %d
             Current streak:             %d days
            
            Keep up the great work! Log in to review your flashcards.
            
            — The EduCircle Team
            https://educircle.app
            """,
                user.getName(),
                sessions,
                studyMin,
                totalNotes,
                dueCards,
                user.getStudyStreak()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("📊 Your EduCircle Weekly Report");
        message.setText(body);
        mailSender.send(message);
        log.debug("Weekly report sent to {}", user.getEmail());
    }
}