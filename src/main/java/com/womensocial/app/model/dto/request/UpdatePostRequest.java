package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private String imageUrl;

    private List<String> topicTags;
}
