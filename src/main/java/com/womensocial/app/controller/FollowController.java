package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.FollowRequest;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.FollowResponse;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping
    @Operation(summary = "Send a follow request")
    public ResponseEntity<ApiResponse<FollowResponse>> sendFollowRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FollowRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        FollowResponse response = followService.sendFollowRequest(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Follow request sent", response));
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Accept a follow request")
    public ResponseEntity<ApiResponse<Void>> acceptFollowRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        followService.acceptFollowRequest(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Follow request accepted", null));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a follow request")
    public ResponseEntity<ApiResponse<Void>> rejectFollowRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        followService.rejectFollowRequest(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Follow request rejected", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Unfollow or cancel a pending follow request")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        followService.unfollow(id, Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Unfollowed", null));
    }

    @GetMapping("/requests")
    @Operation(summary = "Get pending follow requests for the current user")
    public ResponseEntity<ApiResponse<List<FollowResponse>>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(followService.getPendingRequests(userId)));
    }

    @GetMapping("/status/{targetUserId}")
    @Operation(summary = "Get follow status between current user and target")
    public ResponseEntity<ApiResponse<Map<String, String>>> getFollowStatus(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String status = followService.getFollowStatus(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", status)));
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "Get followers of a user")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.getFollowers(userId)));
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "Get users that a user follows")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.getFollowing(userId)));
    }
}
