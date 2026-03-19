package com.EduCircle.Langchain.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardResponse {

    private Long id;
    private Long noteId;
    private String  question;
    private String answer;
    private int repetitions;
    private double easeFactor;
    private int intervalDays;
    private LocalDate nextReviewDate;
    private boolean isDueToday;
}
