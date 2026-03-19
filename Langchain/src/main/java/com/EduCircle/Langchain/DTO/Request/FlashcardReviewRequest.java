package com.EduCircle.Langchain.DTO.Request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class FlashcardReviewRequest {

    @NotNull
    @Min(value = 0,message = "Quality must be 0-5")
    @Max(value = 5,message = "Quality must be 0-5")
    private Integer quality;
}
