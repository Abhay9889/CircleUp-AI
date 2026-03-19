package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.config.RateLimitConfig;
import com.EduCircle.Langchain.Service.SearchService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService    searchService;
    private final RateLimitConfig  rateLimitConfig;

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int count,
            @AuthenticationPrincipal UserDetails userDetails) {

        Bucket bucket = rateLimitConfig.getSearchBucket(userDetails.getUsername());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error",   "Search rate limit exceeded",
                            "message", "You can perform 50 searches per hour"
                    ));
        }

        return ResponseEntity.ok(searchService.search(q, Math.min(count, 20)));
    }
}