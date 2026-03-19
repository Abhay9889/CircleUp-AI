package com.EduCircle.Langchain.DTO.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateQuizRequest {

    @Min(1) @Max(30)
    private int questionCount = 10;

    @NotBlank
    private String quizType = "MCQ";
}