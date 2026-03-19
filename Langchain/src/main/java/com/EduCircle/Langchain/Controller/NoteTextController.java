package com.EduCircle.Langchain.Controller;

import com.EduCircle.Langchain.Entity.Note;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.Noterepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Map;

/**
 * Internal endpoint used by FastAPI to fetch extracted note text.
 * Called as: GET http://springboot:8080/api/notes/{id}/text
 * No auth required (internal Docker network only).
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Slf4j
public class NoteTextController {

    private final Noterepository noteRepository;
    private final MinioClient    minioClient;

    @Value("${minio.bucket-notes}")
    private String bucket;

    @GetMapping("/{id}/text")
    public Map<String, Object> getNoteText(@PathVariable Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + id));

        if (note.getFileKey() == null) {
            return Map.of("text", "", "noteId", id, "title", note.getTitle());
        }

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(note.getFileKey())
                        .build())) {

            // Apache Tika auto-detects and extracts text from PDF/DOCX/PPTX/TXT
            BodyContentHandler handler = new BodyContentHandler(-1); // -1 = no limit
            Metadata metadata         = new Metadata();
            new AutoDetectParser().parse(stream, handler, metadata);
            String text = handler.toString().trim();

            log.debug("Extracted {} chars from note {}", text.length(), id);
            return Map.of(
                    "text",    text,
                    "noteId",  id,
                    "title",   note.getTitle(),
                    "fileType", note.getFileType() != null ? note.getFileType() : ""
            );

        } catch (Exception e) {
            log.error("Text extraction failed for note {}: {}", id, e.getMessage());
            return Map.of("text", "", "noteId", id, "error", e.getMessage());
        }
    }
}
