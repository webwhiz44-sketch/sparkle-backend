package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateAnonymousPostRequest {

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 10, message = "Cannot have more than 10 topic tags")
    private List<String> topicTags;

    private CreatePollRequest poll;
}
