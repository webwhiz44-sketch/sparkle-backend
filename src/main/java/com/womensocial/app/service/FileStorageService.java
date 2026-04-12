package com.womensocial.app.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.womensocial.app.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String GCS_BASE_URL = "https://storage.googleapis.com";

    private final Storage gcsStorage;

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    /**
     * Upload a post image. Returns a public GCS URL.
     */
    public String storePostImage(MultipartFile file) {
        return upload(file, "posts");
    }

    /**
     * Upload a user profile image. Returns a public GCS URL.
     */
    public String storeProfileImage(MultipartFile file) {
        return upload(file, "profiles");
    }

    /**
     * Upload a community cover image. Returns a public GCS URL.
     */
    public String storeCommunityImage(MultipartFile file) {
        return upload(file, "communities");
    }

    /**
     * Generic upload — kept for backward compatibility with FileUploadController.
     */
    public String storeImage(MultipartFile file) {
        return upload(file, "general");
    }

    private String upload(MultipartFile file, String folder) {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String objectName = folder + "/" + UUID.randomUUID() + "." + extension;

        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setCacheControl("public, max-age=31536000") // 1 year cache
                    .build();

            gcsStorage.create(blobInfo, file.getBytes());
            log.info("Uploaded to GCS: {}/{}", bucketName, objectName);

            return String.format("%s/%s/%s", GCS_BASE_URL, bucketName, objectName);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, GIF, and WebP images are allowed");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
