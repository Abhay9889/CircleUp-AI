package com.EduCircle.Langchain.Service;

import com.EduCircle.Langchain.DTO.Response.FlashcardResponse;
import com.EduCircle.Langchain.Entity.Flashcard;
import com.EduCircle.Langchain.Entity.Note;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.Flashcardrepository;
import com.EduCircle.Langchain.Repository.Noterepository;
import com.EduCircle.Langchain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardService {

    private final Flashcardrepository flashcardRepository;
    private final UserRepository userRepository;
    private final Noterepository noteRepository;
    private final WebClient aiWebClient;

    @Transactional
    public FlashcardResponse review(Long cardId, int quality, String userEmail) {
        User user = getUser(userEmail);
        Flashcard card = flashcardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
        if (quality >= 3) {
            int newInterval = switch (card.getRepetitions()) {
                case 0  -> 1;
                case 1  -> 6;
                default -> card.getEaseFactor()
                        .multiply(BigDecimal.valueOf(card.getIntervalDays()))
                        .intValue();
            };
            card.setIntervalDays(newInterval);
            card.setRepetitions(card.getRepetitions() + 1);
        } else {
            card.setRepetitions(0);
            card.setIntervalDays(1);
        }

// Calculate new EF
        BigDecimal currentEF = card.getEaseFactor() != null
                ? card.getEaseFactor()
                : BigDecimal.valueOf(2.5);

        BigDecimal newEF = currentEF.add(
                BigDecimal.valueOf(0.1).subtract(
                        BigDecimal.valueOf(5 - quality).multiply(
                                BigDecimal.valueOf(0.08).add(
                                        BigDecimal.valueOf(5 - quality).multiply(BigDecimal.valueOf(0.02))
                                )
                        )
                )
        );

        card.setEaseFactor(newEF.max(BigDecimal.valueOf(1.3)));

        card.setNextReviewDate(LocalDate.now().plusDays(card.getIntervalDays()));

        Flashcard saved = flashcardRepository.save(card);
        log.debug("Card {} reviewed with quality {}, next review: {}", cardId, quality, saved.getNextReviewDate());
        return toResponse(saved);
    }

    public List<FlashcardResponse> getDueCards(String userEmail) {
        User user = getUser(userEmail);
        return flashcardRepository
                .findByUserIdAndNextReviewDateLessThanEqual(user.getId(), LocalDate.now())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<FlashcardResponse> getAllCards(String userEmail) {
        User user = getUser(userEmail);
        return flashcardRepository.findByUserIdOrderByNextReviewDateAsc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<FlashcardResponse> getCardsByNote(Long noteId, String userEmail) {
        User user = getUser(userEmail);
        return flashcardRepository.findByUserIdAndNoteId(user.getId(), noteId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public long getDueCount(String userEmail) {
        User user = getUser(userEmail);
        return flashcardRepository.countByUserIdAndNextReviewDateLessThanEqual(user.getId(), LocalDate.now());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<FlashcardResponse> generateCards(Long noteId, int count, String userEmail) {
        User user = getUser(userEmail);
        Note note = noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        List<Map<String, String>> aiCards = aiWebClient.post()
                .uri("/flashcards/generate")
                .bodyValue(Map.of("note_id", noteId, "count", count))
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (aiCards == null || aiCards.isEmpty()) {
            throw new RuntimeException("AI service returned no flashcards");
        }

        List<Flashcard> cards = aiCards.stream().map(card ->
                Flashcard.builder()
                        .user(user)
                        .note(note)
                        .question(card.get("question"))
                        .answer(card.get("answer"))
                        .build()
        ).collect(Collectors.toList());

        List<Flashcard> saved = flashcardRepository.saveAll(cards);
        log.info("Generated {} flashcards for note {} by {}", saved.size(), noteId, userEmail);
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteCard(Long cardId, String userEmail) {
        User user = getUser(userEmail);
        flashcardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
        flashcardRepository.deleteByIdAndUserId(cardId, user.getId());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public FlashcardResponse toResponse(Flashcard card) {
        return FlashcardResponse.builder()
                .id(card.getId())
                .noteId(card.getNote() != null ? card.getNote().getId() : null)
                .question(card.getQuestion())
                .answer(card.getAnswer())
                .repetitions(card.getRepetitions())
                .easeFactor(card.getEaseFactor() != null ? card.getEaseFactor().doubleValue() : null)
                .intervalDays(card.getIntervalDays())
                .nextReviewDate(card.getNextReviewDate())
                .isDueToday(!card.getNextReviewDate().isAfter(LocalDate.now()))
                .build();
    }
}