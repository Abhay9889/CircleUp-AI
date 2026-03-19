package com.EduCircle.Langchain.Service;

import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.Noterepository;
import com.EduCircle.Langchain.Repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class AIProxyService {

    private final WebClient aiWebClient;
    private final UserRepository userRepository;
    private final Noterepository noteRepository;

    public AIProxyService(
            @Qualifier("aiWebClient") WebClient aiWebClient,
            UserRepository userRepository,
            Noterepository noteRepository) {

        this.aiWebClient = aiWebClient;
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }


    public Map<String, Object> askQuestion(
            Long noteId,
            String question,
            boolean useWebSearch,
            String language,
            String userEmail) {

        User user = getUser(userEmail);

        noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        Map<String, Object> response = aiWebClient.post()
                .uri("/rag/ask")
                .bodyValue(Map.of(
                        "note_id", noteId,
                        "question", question,
                        "use_web_search", useWebSearch,
                        "language", language
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(120))
                .block();

        return response != null ? response :
                Map.of("error", "AI service unavailable");
    }


    public Map<String, Object> webSearch(String query) {

        Map<String, Object> response = aiWebClient.post()
                .uri("/search/web")
                .bodyValue(Map.of("query", query))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .block();

        return response != null ? response :
                Map.of("results", java.util.List.of());
    }


    @Cacheable(value = "summaries", key = "#noteId + '_' + #userEmail")
    public Map<String, Object> summarizeNote(Long noteId, String userEmail) {

        User user = getUser(userEmail);

        noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        Map<String, Object> response = aiWebClient.post()
                .uri("/tools/summarize")
                .bodyValue(Map.of("note_id", noteId))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        return response != null ? response :
                Map.of("summary", "");
    }

    @Cacheable(value = "mindmaps", key = "#noteId + '_' + #userEmail")
    public Map<String, Object> generateMindMap(Long noteId, String userEmail) {

        User user = getUser(userEmail);

        noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        Map<String, Object> response = aiWebClient.post()
                .uri("/tools/mindmap")
                .bodyValue(Map.of("note_id", noteId))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        return response != null ? response :
                Map.of(
                        "nodes", java.util.List.of(),
                        "edges", java.util.List.of()
                );
    }


    @Cacheable(value = "difficulty", key = "#noteId + '_' + #userEmail")
    public Map<String, Object> getDifficulty(Long noteId, String userEmail) {

        User user = getUser(userEmail);

        noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        Map<String, Object> response = aiWebClient.get()
                .uri("/tools/difficulty/" + noteId)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .block();

        return response != null ? response :
                Map.of("difficulty", "Unknown");
    }


    public Map<String, Object> checkPlagiarism(String text1, String text2) {

        Map<String, Object> response = aiWebClient.post()
                .uri("/tools/plagiarism")
                .bodyValue(Map.of(
                        "text1", text1,
                        "text2", text2
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .block();

        return response != null ? response :
                Map.of("similarity_score", 0.0);
    }


    public Map<String, Object> runOcr(Long noteId, String userEmail) {

        User user = getUser(userEmail);

        noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        Map<String, Object> response = aiWebClient.post()
                .uri("/tools/ocr")
                .bodyValue(Map.of("note_id", noteId))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        return response != null ? response :
                Map.of("text", "");
    }


    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}