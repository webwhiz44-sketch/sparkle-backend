package com.womensocial.app.model.dto.request;

import com.womensocial.app.model.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportRequest {

    @NotNull(message = "Report reason is required")
    private ReportReason reason;

    private String description;

    private Long reportedUserId;
    private Long postId;
    private Long anonymousPostId;
    private Long commentId;
}
