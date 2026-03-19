package com.EduCircle.Langchain.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final WebClient genericWebClient;

    @Value("${brave.search.api-key:}")
    private String braveApiKey;

    @Value("${brave.search.base-url}")
    private String braveBaseUrl;

    @Cacheable(value = "searches", key = "#query + ':' + #count")
    public Map<String, Object> search(String query, int count) {
        if (braveApiKey == null || braveApiKey.isBlank()) {
            log.warn("Brave API key not configured");
            return Map.of("results", List.of(), "error", "Search not configured");
        }

        try {
            Map<?, ?> raw = genericWebClient.get()
                    .uri(braveBaseUrl + "?q={q}&count={c}", query, count)
                    .header("X-Subscription-Token", braveApiKey)
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (raw == null) return Map.of("results", List.of());

//            List<?> webResults = (List<?>) ((Map<?, ?>) raw.getOrDefault("web", Map.of()))
//                    .getOrDefault("results", List.of());

            List<Map<String, String>> results = new ArrayList<>();
//            for (Object r : webResults) {
//                if (r instanceof Map<?, ?> item) {
//                    results.add(Map.of(
//                            "title",       String.valueOf(item.getOrDefault("title", " ")),
//                            "url",         String.valueOf(item.getOrDefault("url", " ")),
//                            "description", String.valueOf(item.getOrDefault("description", ""))
//                    ));
//                }
//            }

            log.debug("Brave search '{}' returned {} results", query, results.size());
            return Map.of("results", results, "query", query);

        } catch (Exception e) {
            log.error("Brave search failed: {}", e.getMessage());
            return Map.of("results", List.of(), "error", "Search failed");
        }
    }
}