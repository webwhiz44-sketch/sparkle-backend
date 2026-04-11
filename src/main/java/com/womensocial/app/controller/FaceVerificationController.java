package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.VerifyFaceRequest;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.CreateLivenessSessionResponse;
import com.womensocial.app.model.dto.response.FaceVerificationResult;
import com.womensocial.app.service.FaceVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/face-verification")
@RequiredArgsConstructor
@Tag(name = "Face Verification")
public class FaceVerificationController {

    private final FaceVerificationService faceVerificationService;

    @PostMapping("/session")
    @Operation(summary = "Create a face liveness session",
               description = "Returns a sessionId to be passed to the AWS Amplify FaceLivenessDetector component on the frontend.")
    public ResponseEntity<ApiResponse<CreateLivenessSessionResponse>> createSession() {
        String sessionId = faceVerificationService.createLivenessSession();
        return ResponseEntity.ok(ApiResponse.success("Session created",
                new CreateLivenessSessionResponse(sessionId)));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify face liveness and gender",
               description = "Checks liveness confidence and gender detection. On success returns a faceVerificationToken to include in the signup request.")
    public ResponseEntity<ApiResponse<FaceVerificationResult>> verify(
            @Valid @RequestBody VerifyFaceRequest request) {
        String token = faceVerificationService.verifyAndIssueToken(request.getSessionId());
        return ResponseEntity.ok(ApiResponse.success("Face verified successfully",
                new FaceVerificationResult(token)));
    }
}
