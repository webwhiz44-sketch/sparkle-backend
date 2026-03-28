package com.womensocial.app.controller;

import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Tag(name = "File Upload")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/image")
    @Operation(summary = "Upload an image, returns URL")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeImage(file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded", Map.of("url", url)));
    }
}
