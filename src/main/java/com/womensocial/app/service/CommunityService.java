package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.model.dto.request.CreateCommunityRequest;
import com.womensocial.app.model.dto.response.CommunityResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.entity.Community;
import com.womensocial.app.model.entity.CommunityMember;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.model.enums.MemberRole;
import com.womensocial.app.model.enums.TopicCategory;
import com.womensocial.app.repository.CommunityMemberRepository;
import com.womensocial.app.repository.CommunityRepository;
import com.womensocial.app.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserService userService;

    @Transactional
    public CommunityResponse createCommunity(Long userId, CreateCommunityRequest request) {
        if (communityRepository.existsByName(request.getName())) {
            throw new BadRequestException("Community name already exists");
        }

        User user = userService.findUserById(userId);
        Community community = Community.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : TopicCategory.GENERAL)
                .coverImageUrl(request.getCoverImageUrl())
                .createdBy(user)
                .memberCount(1)
                .build();

        community = communityRepository.save(community);

        // Creator auto-joins as ADMIN
        communityMemberRepository.save(CommunityMember.builder()
                .community(community)
                .user(user)
                .role(MemberRole.ADMIN)
                .build());

        CommunityResponse response = CommunityResponse.from(community);
        response.setMember(true);
        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommunityResponse> listCommunities(Long userId, TopicCategory category, int page, int size) {
        Page<Community> communities;
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "memberCount"));

        if (category != null) {
            communities = communityRepository.findByCategory(category, pageRequest);
        } else {
            communities = communityRepository.findAll(pageRequest);
        }

        return PagedResponse.<CommunityResponse>builder()
                .content(communities.getContent().stream()
                        .map(c -> enrichWithMemberStatus(CommunityResponse.from(c), userId))
                        .toList())
                .page(communities.getNumber())
                .size(communities.getSize())
                .totalElements(communities.getTotalElements())
                .totalPages(communities.getTotalPages())
                .last(communities.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunity(Long communityId, Long userId) {
        Community community = findById(communityId);
        return enrichWithMemberStatus(CommunityResponse.from(community), userId);
    }

    @Transactional
    public void joinCommunity(Long communityId, Long userId) {
        if (communityMemberRepository.existsByCommunityIdAndUserId(communityId, userId)) {
            throw new BadRequestException("Already a member of this community");
        }
        Community community = findById(communityId);
        User user = userService.findUserById(userId);
        communityMemberRepository.save(CommunityMember.builder()
                .community(community)
                .user(user)
                .role(MemberRole.MEMBER)
                .build());
        community.setMemberCount(community.getMemberCount() + 1);
        communityRepository.save(community);
    }

    @Transactional
    public void leaveCommunity(Long communityId, Long userId) {
        CommunityMember member = communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new BadRequestException("Not a member of this community"));
        if (member.getRole() == MemberRole.ADMIN) {
            throw new BadRequestException("Community admin cannot leave. Transfer ownership first.");
        }
        communityMemberRepository.delete(member);
        Community community = findById(communityId);
        community.setMemberCount(Math.max(0, community.getMemberCount() - 1));
        communityRepository.save(community);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommunityResponse> getMyCommunities(Long userId, int page, int size) {
        Page<CommunityMember> memberships = communityMemberRepository.findByUserId(userId,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.DESC, "joinedAt")));

        return PagedResponse.<CommunityResponse>builder()
                .content(memberships.getContent().stream()
                        .map(cm -> {
                            CommunityResponse r = CommunityResponse.from(cm.getCommunity());
                            r.setMember(true);
                            return r;
                        })
                        .toList())
                .page(memberships.getNumber())
                .size(memberships.getSize())
                .totalElements(memberships.getTotalElements())
                .totalPages(memberships.getTotalPages())
                .last(memberships.isLast())
                .build();
    }

    private CommunityResponse enrichWithMemberStatus(CommunityResponse response, Long userId) {
        if (userId != null) {
            response.setMember(communityMemberRepository.existsByCommunityIdAndUserId(response.getId(), userId));
        }
        return response;
    }

    public Community findById(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + communityId));
    }
}
