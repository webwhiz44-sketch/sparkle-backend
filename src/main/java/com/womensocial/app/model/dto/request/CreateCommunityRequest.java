package com.womensocial.app.model.dto.request;

import com.womensocial.app.model.enums.TopicCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommunityRequest {

    @NotBlank(message = "Community name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    private String description;

    private TopicCategory category;

    private String coverImageUrl;
}
