package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.CreateCommunityRequest;
import com.womensocial.app.model.dto.response.*;
import com.womensocial.app.model.enums.TopicCategory;
import com.womensocial.app.service.CommunityService;
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
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Tag(name = "Communities")
public class CommunityController {

    private final CommunityService communityService;
    private final PostService postService;

    @PostMapping
    @Operation(summary = "Create a community")
    public ResponseEntity<ApiResponse<CommunityResponse>> createCommunity(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCommunityRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Community created", communityService.createCommunity(userId, request)));
    }

    @GetMapping
    @Operation(summary = "List communities, optionally filter by category")
    public ResponseEntity<ApiResponse<PagedResponse<CommunityResponse>>> listCommunities(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TopicCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(communityService.listCommunities(userId, category, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get community details")
    public ResponseEntity<ApiResponse<CommunityResponse>> getCommunity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(communityService.getCommunity(id, userId)));
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join a community")
    public ResponseEntity<ApiResponse<Void>> joinCommunity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        communityService.joinCommunity(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Joined community", null));
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Leave a community")
    public ResponseEntity<ApiResponse<Void>> leaveCommunity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        communityService.leaveCommunity(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Left community", null));
    }

    @GetMapping("/{id}/posts")
    @Operation(summary = "Get community feed")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getCommunityPosts(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(postService.getCommunityPosts(id, userId, page, size)));
    }
}
