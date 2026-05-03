package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateStoryRequest {

    @NotBlank
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotBlank
    @Size(max = 10000, message = "Story cannot exceed 10,000 characters")
    private String body;

    private String coverImageUrl;

    private List<String> tags = new ArrayList<>();
}
