package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.CreateCommentRequest;
import com.womensocial.app.model.dto.request.CreateStoryRequest;
import com.womensocial.app.model.dto.request.UpdateStoryRequest;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.CommentResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.dto.response.StoryResponse;
import com.womensocial.app.service.CommentService;
import com.womensocial.app.service.StoryService;
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
@RequestMapping("/api/stories")
@RequiredArgsConstructor
@Tag(name = "Radiant Stories")
public class StoryController {

    private final StoryService storyService;
    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Create a story")
    public ResponseEntity<ApiResponse<StoryResponse>> createStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateStoryRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Story created", storyService.createStory(userId, request)));
    }

    @GetMapping
    @Operation(summary = "Get stories feed")
    public ResponseEntity<ApiResponse<PagedResponse<StoryResponse>>> getFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(storyService.getFeed(userId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single story")
    public ResponseEntity<ApiResponse<StoryResponse>> getStory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(storyService.getStory(id, userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit own story")
    public ResponseEntity<ApiResponse<StoryResponse>> updateStory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateStoryRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Story updated", storyService.updateStory(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own story")
    public ResponseEntity<ApiResponse<Void>> deleteStory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        storyService.deleteStory(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Story deleted", null));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a story")
    public ResponseEntity<ApiResponse<Void>> likeStory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        storyService.likeStory(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Unlike a story")
    public ResponseEntity<ApiResponse<Void>> unlikeStory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        storyService.unlikeStory(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments on a story")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(commentService.getStoryComments(id, userId, page, size)));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Comment on a story")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", commentService.commentOnStory(id, userId, request)));
    }
}
