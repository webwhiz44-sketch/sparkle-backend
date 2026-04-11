package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.model.entity.FaceVerificationToken;
import com.womensocial.app.repository.FaceVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FaceVerificationService {

    private final RekognitionClient rekognitionClient;
    private final FaceVerificationTokenRepository tokenRepository;

    @Value("${face.verification.liveness-confidence-threshold:90.0}")
    private float livenessThreshold;

    @Value("${face.verification.gender-confidence-threshold:85.0}")
    private float genderThreshold;

    @Value("${face.verification.token-expiry-minutes:15}")
    private int tokenExpiryMinutes;

    /**
     * Creates a new AWS Rekognition Face Liveness session.
     * The sessionId is returned to the frontend, which uses the
     * AWS Amplify FaceLivenessDetector component to run the challenge.
     */
    public String createLivenessSession() {
        try {
            CreateFaceLivenessSessionResponse response = rekognitionClient.createFaceLivenessSession(
                    CreateFaceLivenessSessionRequest.builder()
                            .settings(CreateFaceLivenessSessionRequestSettings.builder()
                                    .auditImagesLimit(1)
                                    .build())
                            .build()
            );
            return response.sessionId();
        } catch (RekognitionException e) {
            throw new BadRequestException("Failed to create liveness session: " + e.getMessage());
        }
    }

    /**
     * Fetches liveness session results, checks confidence threshold,
     * then runs gender detection on the captured face image.
     * On success, issues a short-lived face verification token to be
     * included in the signup request.
     */
    @Transactional
    public String verifyAndIssueToken(String sessionId) {
        GetFaceLivenessSessionResultsResponse results;
        try {
            results = rekognitionClient.getFaceLivenessSessionResults(
                    GetFaceLivenessSessionResultsRequest.builder()
                            .sessionId(sessionId)
                            .build()
            );
        } catch (RekognitionException e) {
            throw new BadRequestException("Failed to retrieve liveness results: " + e.getMessage());
        }

        // 1. Liveness check
        if (results.status() != LivenessSessionStatus.SUCCEEDED) {
            throw new BadRequestException("Liveness check did not pass. Please retry.");
        }
        if (results.confidence() < livenessThreshold) {
            throw new BadRequestException("Liveness confidence too low. Please retry in better lighting.");
        }

        // 2. Get the captured face image from audit images
        List<AuditImage> auditImages = results.auditImages();
        if (auditImages == null || auditImages.isEmpty()) {
            throw new BadRequestException("No face image captured during liveness check.");
        }
        AuditImage capturedFace = auditImages.get(0);

        // 3. Gender detection on the captured frame
        DetectFacesResponse detectResponse;
        try {
            detectResponse = rekognitionClient.detectFaces(
                    DetectFacesRequest.builder()
                            .image(Image.builder().bytes(capturedFace.bytes()).build())
                            .attributes(List.of(Attribute.ALL))
                            .build()
            );
        } catch (RekognitionException e) {
            throw new BadRequestException("Face analysis failed: " + e.getMessage());
        }

        if (detectResponse.faceDetails().isEmpty()) {
            throw new BadRequestException("No face detected. Please ensure your face is clearly visible.");
        }

        FaceDetail faceDetail = detectResponse.faceDetails().get(0);
        Gender gender = faceDetail.gender();

        if (gender.value() != GenderType.FEMALE || gender.confidence() < genderThreshold) {
            throw new BadRequestException("Verification failed: this app is exclusively for women.");
        }

        // 4. Issue a short-lived, single-use verification token
        String token = UUID.randomUUID().toString();
        tokenRepository.save(FaceVerificationToken.builder()
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                .build());

        return token;
    }

    /**
     * Validates and consumes a face verification token during signup.
     * Throws BadRequestException if the token is invalid, used, or expired.
     */
    @Transactional
    public void consumeToken(String token) {
        FaceVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid face verification token."));

        if (verificationToken.getUsed()) {
            throw new BadRequestException("Face verification token has already been used.");
        }
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Face verification token has expired. Please verify again.");
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);
    }
}
