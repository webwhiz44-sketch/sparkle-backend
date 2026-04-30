package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateStoryRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotBlank
    private String body;

    private String coverImageUrl;

    private List<String> tags = new ArrayList<>();
}
