package com.womensocial.app.controller;

import com.womensocial.app.model.dto.request.CreateReportRequest;
import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.dto.response.UserResponse;
import com.womensocial.app.service.ReportService;
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
@Tag(name = "Reporting & Safety")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/api/reports")
    @Operation(summary = "Report a post, comment, or user")
    public ResponseEntity<ApiResponse<Void>> createReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReportRequest request) {
        reportService.createReport(Long.parseLong(userDetails.getUsername()), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report submitted", null));
    }

    @PostMapping("/api/blocks/{userId}")
    @Operation(summary = "Block a user")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        reportService.blockUser(Long.parseLong(userDetails.getUsername()), userId);
        return ResponseEntity.ok(ApiResponse.success("User blocked", null));
    }

    @DeleteMapping("/api/blocks/{userId}")
    @Operation(summary = "Unblock a user")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        reportService.unblockUser(Long.parseLong(userDetails.getUsername()), userId);
        return ResponseEntity.ok(ApiResponse.success("User unblocked", null));
    }

    @GetMapping("/api/blocks")
    @Operation(summary = "List blocked users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getBlockedUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(reportService.getBlockedUsers(userId, page, size)));
    }
}
