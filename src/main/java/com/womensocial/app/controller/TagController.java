package com.womensocial.app.controller;

import com.womensocial.app.model.dto.response.ApiResponse;
import com.womensocial.app.repository.AnonymousPostRepository;
import com.womensocial.app.repository.PostRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags")
public class TagController {

    private final PostRepository postRepository;
    private final AnonymousPostRepository anonymousPostRepository;

    @GetMapping("/trending")
    @Operation(summary = "Get top 10 trending tags from the last 7 days")
    public ResponseEntity<ApiResponse<List<String>>> getTrendingTags() {
        List<String> tags = postRepository.findTrendingTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}
