package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public ResponseEntity<Map<String, String>> translate(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal UserDetails userDetails) {
        String text           = req.get("text");
        String targetLanguage = req.get("targetLanguage");
        String translated     = translationService.translate(text, targetLanguage);
        return ResponseEntity.ok(Map.of(
                "original",        text,
                "translated",      translated,
                "targetLanguage",  targetLanguage
        ));
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> supportedLanguages() {
        return ResponseEntity.ok(translationService.getSupportedLanguages());
    }
}

