package com.EduCircle.Langchain.DTO.Response;

import com.EduCircle.Langchain.Entity.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {

    private Long id;
    private String title;
    private String fileType;
    private Long fileSizeBytes;
    private String summary;
    private Double difficultyScore;
    private String[] tags;
    private String language;
    private Note.ProcessingStatus processingStatus;
    private LocalDateTime updatedAt;
}
