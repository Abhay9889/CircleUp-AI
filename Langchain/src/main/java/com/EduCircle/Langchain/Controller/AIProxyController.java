package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Service.AIProxyService;
import com.EduCircle.Langchain.config.RateLimitConfig;
//import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.github.bucket4j.Bucket;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIProxyController {

    private final AIProxyService aiProxyService;
    private final RateLimitConfig rateLimitConfig;

    @PostMapping("/ask")
    public ResponseEntity<Map<String,Object>>ask(@RequestBody Map<String,Object>req, @AuthenticationPrincipal UserDetails userDetails){
        Bucket bucket= (Bucket) rateLimitConfig.getAiBucket(userDetails.getUsername());
    if(!bucket.tryConsume(1)){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error","Rate limit exceeded . "));
    }
    Long noteId=Long.valueOf(req.get("noteId").toString());
    String question= req.get("question".toString()).toString();
    boolean useWebSearch = Boolean.parseBoolean(req.getOrDefault("useWebSearch", "false").toString());
    String language=req.getOrDefault("language","english").toString();
    return ResponseEntity.ok(aiProxyService.askQuestion(noteId,question,useWebSearch,language,userDetails.getUsername()));
     }

     @PostMapping("/search")
    public ResponseEntity<Map<String,Object>>search(@RequestBody Map<String,Object>req,@AuthenticationPrincipal UserDetails userDetails){
         Bucket bucket = rateLimitConfig.getSearchBucket(userDetails.getUsername());
         if (!bucket.tryConsume(1)) {
             return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                     .body(Map.of("error", "Search rate limit exceeded."));
         }
         return ResponseEntity.ok(
                 aiProxyService.webSearch((String) req.get("query"))
         );
     }

    @PostMapping("/summarize/{noteId}")
    public ResponseEntity<Map<String, Object>> summarize(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                aiProxyService.summarizeNote(noteId, userDetails.getUsername())
        );
    }
    @PostMapping("/mindmap/{noteId}")
    public ResponseEntity<Map<String, Object>> mindmap(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                aiProxyService.generateMindMap(noteId, userDetails.getUsername())
        );
    }
    @GetMapping("/difficulty/{noteId}")
    public ResponseEntity<Map<String, Object>> difficulty(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                aiProxyService.getDifficulty(noteId, userDetails.getUsername())
        );
    }
    @PostMapping("/plagiarism")
    public ResponseEntity<Map<String, Object>> plagiarism(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                aiProxyService.checkPlagiarism(req.get("text1"), req.get("text2"))
        );
    }
    @PostMapping("/ocr/{noteId}")
    public ResponseEntity<Map<String, Object>> ocr(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                aiProxyService.runOcr(noteId, userDetails.getUsername())
        );
    }
}
