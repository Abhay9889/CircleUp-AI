package com.EduCircle.Langchain.DTO.Request;

import lombok.Data;

@Data
public class RestoreRequest {
    private String email;
    private String newPassword;
    private String confirmPassword;
    private String resetToken;
}