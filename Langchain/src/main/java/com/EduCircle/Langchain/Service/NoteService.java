package com.EduCircle.Langchain.Service;

import com.EduCircle.Langchain.DTO.Response.NoteResponse;
import com.EduCircle.Langchain.Entity.Note;
import com.EduCircle.Langchain.Entity.User;
import com.EduCircle.Langchain.Exception.ResourceNotFoundException;
import com.EduCircle.Langchain.Repository.Noterepository;
import com.EduCircle.Langchain.Repository.UserRepository;
import io.minio.*;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoteService {
    private final Noterepository noterepository;
    private final UserRepository userRepository;
    private final MinioClient minioClient;
    private final WebClient aiWebClient;

    @Value("${minio.bucket-notes}")
    private String bucket;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    public NoteService(
            Noterepository noterepository,
            UserRepository userRepository,
            MinioClient minioClient,
            @Qualifier("aiWebClient") WebClient aiWebClient) {
        this.noterepository = noterepository;
        this.userRepository = userRepository;
        this.minioClient    = minioClient;
        this.aiWebClient    = aiWebClient;
    }

    @Transactional
    public NoteResponse uploadNotes(String title, MultipartFile file, String userEmail) {
        validateFile(file);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!!"));

        String ext     = getExtension(file.getOriginalFilename());
        String fileKey = "notes/" + user.getId() + "/" + UUID.randomUUID() + "." + ext;

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + e.getMessage());
        }

        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }

        Note note = Note.builder()
                .user(user)
                .title(title)
                .fileKey(fileKey)
                .fileSizeBytes(file.getSize())
                .fileType(file.getContentType())
                .processingStatus(Note.ProcessingStatus.PENDING)
                .build();
        Note saved = noterepository.save(note);

        final Long noteId        = saved.getId();
        final String contentType = file.getContentType();
        final String origName    = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "file." + ext;

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileBytes) {
                @Override public String getFilename() { return origName; }
            }).contentType(org.springframework.http.MediaType.parseMediaType(
                    contentType != null ? contentType : "application/octet-stream"));
            builder.part("file_type", contentType != null ? contentType : "text/plain");

            aiWebClient.post()
                    .uri("/rag/index/" + noteId)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            res -> {
                                noterepository.findById(noteId).ifPresent(n -> {
                                    n.setProcessingStatus(Note.ProcessingStatus.READY);
                                    noterepository.save(n);
                                    log.info("Note {} indexed and marked READY", noteId);
                                });
                            },
                            err -> log.warn("FastAPI indexing failed for note {}: {}", noteId, err.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Could not trigger FastAPI indexing for note {}: {}", noteId, e.getMessage());
        }

        log.info("Note uploaded: {} by {}", title, userEmail);
        return toResponse(saved);
    }

    public List<NoteResponse> getUserNotes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!!"));

        return noterepository.findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public NoteResponse getNote(Long noteId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!!"));
        Note note = noterepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found!!"));
        return toResponse(note);
    }

    @Transactional
    public void deleteNote(Long noteId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!!"));
        Note note = noterepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found!!"));

        if (note.getFileKey() != null) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(note.getFileKey())
                        .build());
            } catch (Exception e) {
                log.warn("Could not delete file from MinIO: {}", e.getMessage());
            }
        }

        // Also delete FAISS index
        try {
            aiWebClient.delete()
                    .uri("/rag/index/" + noteId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            res -> log.info("FAISS index deleted for note {}", noteId),
                            err -> log.warn("Could not delete FAISS index for note {}: {}", noteId, err.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Could not trigger FAISS deletion for note {}: {}", noteId, e.getMessage());
        }

        noterepository.delete(note);
        log.info("Note {} deleted by {}", noteId, userEmail);
    }

    public String getPresignedDownloadUrl(Long noteId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Note note = noterepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(note.getFileKey())
                    .expiry(15, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Could not generate download URL: " + e.getMessage());
        }
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getSize() > 50 * 1024 * 1024) throw new IllegalArgumentException("File too large (max 50MB)");
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type: " + file.getContentType());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    public NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .fileType(note.getFileType())
                .fileSizeBytes(note.getFileSizeBytes())
                .summary(note.getSummary())
                .difficultyScore(note.getDifficultyScore())
                .tags(note.getTags())
                .language(note.getLanguage())
                .processingStatus(note.getProcessingStatus())
                .updatedAt(note.getUploadedAt())
                .build();
    }
}