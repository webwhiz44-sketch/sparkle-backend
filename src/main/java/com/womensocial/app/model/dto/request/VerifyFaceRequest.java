package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyFaceRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;
}
