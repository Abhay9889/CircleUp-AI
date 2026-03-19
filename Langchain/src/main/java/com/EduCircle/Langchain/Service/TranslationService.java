package com.EduCircle.Langchain.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    private final WebClient aiWebClint;

    private static final List<String>SUPPORTED_LANGUAGES=List.of(
            "hindi", "tamil", "telugu", "bengali", "marathi", "gujarati",
            "kannada", "malayalam", "punjabi", "urdu",
            "french", "german", "spanish", "arabic",
            "chinese", "japanese", "korean", "russian"
    );

    @Cacheable(value = "translations",key ="#text.hashCode() + '_' + #targetLanguage" )
    public String translate(String text,String targetLanguage){
        if(targetLanguage==null || targetLanguage.equalsIgnoreCase("english")){
            return text;
        }
        try {
            Map<String,Object>response=aiWebClint.post().uri("/translate").bodyValue(Map.of(
                    "text",            text,
                    "target_language", targetLanguage
            )).retrieve().bodyToMono(Map.class).block();
            if (response!=null && response.containsKey("translated")){
                return response.get("translated").toString();
            }
        }
        catch (Exception e){
            log.error("Translation failed for language {} : {} ",targetLanguage,e.getMessage());
        }
        return text;
    }
    public List<String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }
}
