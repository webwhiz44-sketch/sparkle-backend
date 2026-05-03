package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Post cannot exceed 1000 characters")
    private String content;

    private Long communityId;

    private String imageUrl;

    @Size(max = 10, message = "Cannot have more than 10 topic tags")
    private List<String> topicTags;

    private CreatePollRequest poll;
}
