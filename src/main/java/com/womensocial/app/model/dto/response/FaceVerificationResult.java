package com.womensocial.app.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FaceVerificationResult {
    /**
     * Short-lived single-use token to be included in the signup request.
     * Expires in 15 minutes (configurable).
     */
    private String faceVerificationToken;
}
