package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.model.entity.FaceVerificationToken;
import com.womensocial.app.repository.FaceVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaceVerificationService {

    private final RekognitionClient rekognitionClient;
    private final FaceVerificationTokenRepository tokenRepository;

    @Value("${face.verification.liveness-confidence-threshold:80.0}")
    private float livenessThreshold;

    @Value("${face.verification.gender-confidence-threshold:70.0}")
    private float genderThreshold;

    @Value("${face.verification.token-expiry-minutes:15}")
    private int tokenExpiryMinutes;

    public String createLivenessSession() {
        try {
            CreateFaceLivenessSessionResponse response = rekognitionClient.createFaceLivenessSession(
                    CreateFaceLivenessSessionRequest.builder()
                            .settings(CreateFaceLivenessSessionRequestSettings.builder()
                                    .auditImagesLimit(1)
                                    .build())
                            .build()
            );
            log.info("[FaceVerification] Session created: {}", response.sessionId());
            return response.sessionId();
        } catch (RekognitionException e) {
            log.error("[FaceVerification] Failed to create session: {}", e.getMessage());
            throw new BadRequestException("Failed to create liveness session: " + e.getMessage());
        }
    }

    @Transactional
    public String verifyAndIssueToken(String sessionId) {
        log.info("[FaceVerification] Starting verify for session: {}", sessionId);

        GetFaceLivenessSessionResultsResponse results;
        try {
            results = rekognitionClient.getFaceLivenessSessionResults(
                    GetFaceLivenessSessionResultsRequest.builder()
                            .sessionId(sessionId)
                            .build()
            );
        } catch (RekognitionException e) {
            log.error("[FaceVerification] Failed to get results for session {}: {}", sessionId, e.getMessage());
            throw new BadRequestException("Failed to retrieve liveness results: " + e.getMessage());
        }

        log.info("[FaceVerification] Session {} — status={}, confidence={}",
                sessionId, results.status(), results.confidence());

        // 1. Liveness check
        if (results.status() != LivenessSessionStatus.SUCCEEDED) {
            log.warn("[FaceVerification] FAILED — status={} (not SUCCEEDED)", results.status());
            throw new BadRequestException("Liveness check did not pass. Please retry.");
        }
        if (results.confidence() < livenessThreshold) {
            log.warn("[FaceVerification] FAILED — liveness confidence={} below threshold={}",
                    results.confidence(), livenessThreshold);
            throw new BadRequestException("Liveness confidence too low. Please retry in better lighting.");
        }

        // 2. Get best-quality face image (referenceImage > auditImages)
        AuditImage capturedFace = results.referenceImage();
        boolean usingReference = capturedFace != null && capturedFace.bytes() != null;
        if (!usingReference) {
            log.warn("[FaceVerification] referenceImage null/empty, falling back to auditImages");
            List<AuditImage> auditImages = results.auditImages();
            log.info("[FaceVerification] auditImages count: {}", auditImages == null ? "null" : auditImages.size());
            if (auditImages == null || auditImages.isEmpty()) {
                throw new BadRequestException("No face image captured during liveness check.");
            }
            capturedFace = auditImages.get(0);
        } else {
            log.info("[FaceVerification] Using referenceImage ({} bytes)", capturedFace.bytes().asByteArray().length);
        }

        // 3. Gender detection
        DetectFacesResponse detectResponse;
        try {
            detectResponse = rekognitionClient.detectFaces(
                    DetectFacesRequest.builder()
                            .image(Image.builder().bytes(capturedFace.bytes()).build())
                            .attributes(List.of(Attribute.ALL))
                            .build()
            );
        } catch (RekognitionException e) {
            log.error("[FaceVerification] DetectFaces failed: {}", e.getMessage());
            throw new BadRequestException("Face analysis failed: " + e.getMessage());
        }

        log.info("[FaceVerification] DetectFaces found {} face(s)", detectResponse.faceDetails().size());

        if (detectResponse.faceDetails().isEmpty()) {
            throw new BadRequestException("No face detected. Please ensure your face is clearly visible.");
        }

        FaceDetail faceDetail = detectResponse.faceDetails().get(0);
        Gender gender = faceDetail.gender();
        log.info("[FaceVerification] Gender={}, confidence={} (threshold={})",
                gender.value(), gender.confidence(), genderThreshold);

        if (gender.value() != GenderType.FEMALE || gender.confidence() < genderThreshold) {
            log.warn("[FaceVerification] FAILED gender check — value={}, confidence={}",
                    gender.value(), gender.confidence());
            throw new BadRequestException("Verification failed: this app is exclusively for women.");
        }

        // 4. Issue token
        String token = UUID.randomUUID().toString();
        tokenRepository.save(FaceVerificationToken.builder()
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                .build());

        log.info("[FaceVerification] SUCCESS — token issued for session {}", sessionId);
        return token;
    }

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
