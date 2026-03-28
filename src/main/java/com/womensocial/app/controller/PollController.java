package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.VoteRequest;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.PollResponse;
import com.womensocial.app.service.PollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
@Tag(name = "Polls")
public class PollController {

    private final PollService pollService;

    @GetMapping("/{id}")
    @Operation(summary = "Get poll with results")
    public ResponseEntity<ApiResponse<PollResponse>> getPoll(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return ResponseEntity.ok(ApiResponse.success(pollService.getPoll(id, userId)));
    }

    @PostMapping("/{id}/vote")
    @Operation(summary = "Vote on a poll")
    public ResponseEntity<ApiResponse<PollResponse>> vote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VoteRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Vote recorded", pollService.vote(id, userId, request)));
    }
}
