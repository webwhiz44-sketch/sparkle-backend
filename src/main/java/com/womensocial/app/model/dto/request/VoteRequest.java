package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {

    @NotNull(message = "Option ID is required")
    private Long optionId;
}
