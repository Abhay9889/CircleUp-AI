package com.EduCircle.Langchain.DTO.Request;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SubmitQuizRequest {
    private List<Map<String, String>> answers;
}