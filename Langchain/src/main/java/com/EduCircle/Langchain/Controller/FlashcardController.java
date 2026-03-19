package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.DTO.Request.FlashcardReviewRequest;
import com.EduCircle.Langchain.DTO.Response.FlashcardResponse;
import com.EduCircle.Langchain.Service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @GetMapping("/due")
    public ResponseEntity<List<FlashcardResponse>> getDueCards(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(flashcardService.getDueCards(userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<FlashcardResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(flashcardService.getAllCards(userDetails.getUsername()));
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<List<FlashcardResponse>> getByNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(flashcardService.getCardsByNote(noteId, userDetails.getUsername()));
    }

    @PostMapping("/generate/{noteId}")
    public ResponseEntity<List<FlashcardResponse>> generate(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "10") int count,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(flashcardService.generateCards(noteId, count, userDetails.getUsername()));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<FlashcardResponse> review(
            @PathVariable Long id,
            @Valid @RequestBody FlashcardReviewRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(flashcardService.review(id, req.getQuality(), userDetails.getUsername()));
    }

    @GetMapping("/due-count")
    public ResponseEntity<Map<String, Long>> dueCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = flashcardService.getDueCount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("dueCount", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        flashcardService.deleteCard(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
