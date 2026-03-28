package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.CreateCommentRequest;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.CommentResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.service.CommentService;
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
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/posts/{postId}/comments")
    @Operation(summary = "Comment on a post")
    public ResponseEntity<ApiResponse<CommentResponse>> commentOnPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", commentService.commentOnPost(postId, userId, request)));
    }

    @PostMapping("/api/anonymous-posts/{postId}/comments")
    @Operation(summary = "Comment on an anonymous post")
    public ResponseEntity<ApiResponse<CommentResponse>> commentOnAnonymousPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", commentService.commentOnAnonymousPost(postId, userId, request)));
    }

    @GetMapping("/api/posts/{postId}/comments")
    @Operation(summary = "Get comments on a post")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getPostComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(commentService.getPostComments(postId, userId, page, size)));
    }

    @GetMapping("/api/anonymous-posts/{postId}/comments")
    @Operation(summary = "Get comments on anonymous post")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getAnonPostComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(commentService.getAnonymousPostComments(postId, userId, page, size)));
    }

    @DeleteMapping("/api/comments/{id}")
    @Operation(summary = "Delete own comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }

    @PostMapping("/api/comments/{id}/like")
    @Operation(summary = "Like a comment")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.likeComment(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/api/comments/{id}/like")
    @Operation(summary = "Unlike a comment")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.unlikeComment(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
