package com.womensocial.app.model.dto.response;

import com.womensocial.app.model.entity.CommunityMember;
import com.womensocial.app.model.enums.MemberRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommunityMemberResponse {

    private Long userId;
    private String displayName;
    private String profileImageUrl;
    private MemberRole role;
    private LocalDateTime joinedAt;

    public static CommunityMemberResponse from(CommunityMember member) {
        return CommunityMemberResponse.builder()
                .userId(member.getUser().getId())
                .displayName(member.getUser().getDisplayName())
                .profileImageUrl(member.getUser().getProfileImageUrl())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
