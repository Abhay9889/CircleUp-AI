package com.EduCircle.Langchain.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "email is required!!")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required!!")
    private String password;
}
