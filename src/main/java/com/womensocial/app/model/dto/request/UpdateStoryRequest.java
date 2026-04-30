package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateStoryRequest {

    @Size(max = 300)
    private String title;

    private String body;
    private String coverImageUrl;
    private List<String> tags;
}
