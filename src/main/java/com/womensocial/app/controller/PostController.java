package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.CreatePostRequest;
import com.womensocial.app.model.dto.request.UpdatePostRequest;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.dto.response.PostResponse;
import com.womensocial.app.service.PostService;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts (Storytime)")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Create a post")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePostRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PostResponse post = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Post created", post));
    }

    @GetMapping
    @Operation(summary = "Get personalized feed, or filter by tag")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) String tag) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        if (tag != null && !tag.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(postService.getPostsByTag(tag, userId, page, size)));
        }
        return ResponseEntity.ok(ApiResponse.success(postService.getFeed(userId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single post")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(postService.getPost(id, userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit own post")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePostRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post updated", postService.updatePost(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.likePost(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.unlikePost(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/save")
    @Operation(summary = "Save (bookmark) a post")
    public ResponseEntity<ApiResponse<Void>> savePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.savePost(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Post saved", null));
    }

    @DeleteMapping("/{id}/save")
    @Operation(summary = "Unsave (remove bookmark) a post")
    public ResponseEntity<ApiResponse<Void>> unsavePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.unsavePost(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Post unsaved", null));
    }

    @GetMapping("/my")
    @Operation(summary = "Get posts by the current user")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(postService.getMyPosts(userId, page, size)));
    }

    @GetMapping("/saved")
    @Operation(summary = "Get all saved posts for the current user")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getSavedPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(postService.getSavedPosts(userId, page, size)));
    }
}
