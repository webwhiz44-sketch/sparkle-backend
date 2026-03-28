package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePollRequest {

    @NotBlank(message = "Poll question is required")
    @Size(max = 500, message = "Question cannot exceed 500 characters")
    private String question;

    @NotEmpty(message = "Poll must have at least 2 options")
    @Size(min = 2, max = 6, message = "Poll must have between 2 and 6 options")
    private List<String> options;
}
