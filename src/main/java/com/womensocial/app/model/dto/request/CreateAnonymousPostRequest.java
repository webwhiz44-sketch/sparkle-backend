package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateAnonymousPostRequest {

    @Size(max = 500, message = "Spill cannot exceed 500 characters")
    private String content;

    private String imageUrl;

    @Size(max = 10, message = "Cannot have more than 10 topic tags")
    private List<String> topicTags;

    private CreatePollRequest poll;
}
