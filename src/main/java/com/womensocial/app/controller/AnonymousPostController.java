package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.CreateAnonymousPostRequest;
import com.womensocial.app.model.dto.response.AnonymousPostResponse;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.service.AnonymousPostService;
import com.womensocial.app.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anonymous-posts")
@RequiredArgsConstructor
@Tag(name = "Spill the Tea (Anonymous Posts)")
public class AnonymousPostController {

    private final AnonymousPostService anonymousPostService;

    @PostMapping
    @Operation(summary = "Create anonymous post")
    public ResponseEntity<ApiResponse<AnonymousPostResponse>> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAnonymousPostRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        AnonymousPostResponse post = anonymousPostService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Post created", post));
    }

    @GetMapping
    @Operation(summary = "Get anonymous feed")
    public ResponseEntity<ApiResponse<PagedResponse<AnonymousPostResponse>>> getFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(anonymousPostService.getFeed(userId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single anonymous post")
    public ResponseEntity<ApiResponse<AnonymousPostResponse>> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(anonymousPostService.getPost(id, userId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own anonymous post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        anonymousPostService.deletePost(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like anonymous post")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        anonymousPostService.likePost(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Unlike anonymous post")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        anonymousPostService.unlikePost(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
