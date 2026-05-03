package com.womensocial.app.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowRequest {

    @NotNull(message = "Target user ID is required")
    private Long targetUserId;
}
