package com.EduCircle.Langchain.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AskQuestionRequest {

    @NotNull
    private Long noteId;

    @NotBlank
    private String question;

    private String language = "english";
}