package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.DTO.Response.NoteResponse;
import com.EduCircle.Langchain.Service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoteResponse> upload(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteService.uploadNotes(title, file, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponse>> list(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.getUserNotes(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.getNote(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        noteService.deleteNote(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download-url")
    public ResponseEntity<Map<String, String>> downloadUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String url = noteService.getPresignedDownloadUrl(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("url", url));
    }
}
