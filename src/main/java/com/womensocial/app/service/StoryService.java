package com.womensocial.app.service;

import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.exception.UnauthorizedException;
import com.womensocial.app.model.dto.request.CreateStoryRequest;
import com.womensocial.app.model.dto.request.UpdateStoryRequest;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.dto.response.StoryResponse;
import com.womensocial.app.model.entity.Like;
import com.womensocial.app.model.entity.Story;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.repository.LikeRepository;
import com.womensocial.app.repository.StoryRepository;
import com.womensocial.app.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;

    @Transactional
    public StoryResponse createStory(Long userId, CreateStoryRequest request) {
        User user = userService.findUserById(userId);
        Story story = Story.builder()
                .user(user)
                .title(request.getTitle())
                .body(request.getBody())
                .coverImageUrl(request.getCoverImageUrl())
                .tags(request.getTags())
                .build();
        return StoryResponse.from(storyRepository.save(story));
    }

    @Transactional(readOnly = true)
    public PagedResponse<StoryResponse> getFeed(Long userId, int page, int size) {
        Page<Story> stories = storyRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE)));
        return toPagedResponse(stories, userId);
    }

    @Transactional(readOnly = true)
    public StoryResponse getStory(Long storyId, Long userId) {
        Story story = findById(storyId);
        return enrichWithLikeStatus(StoryResponse.from(story), userId);
    }

    @Transactional
    public StoryResponse updateStory(Long storyId, Long userId, UpdateStoryRequest request) {
        Story story = findById(storyId);
        if (!story.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own stories");
        }
        if (request.getTitle() != null) story.setTitle(request.getTitle());
        if (request.getBody() != null) story.setBody(request.getBody());
        if (request.getCoverImageUrl() != null) story.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getTags() != null) story.setTags(request.getTags());
        return StoryResponse.from(storyRepository.save(story));
    }

    @Transactional
    public void deleteStory(Long storyId, Long userId) {
        Story story = findById(storyId);
        if (!story.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own stories");
        }
        storyRepository.delete(story);
    }

    @Transactional
    public void likeStory(Long storyId, Long userId) {
        if (likeRepository.existsByUserIdAndStoryId(userId, storyId)) {
            return;
        }
        Story story = findById(storyId);
        User user = userService.findUserById(userId);
        likeRepository.save(Like.builder().user(user).story(story).build());
        story.setLikeCount(story.getLikeCount() + 1);
        storyRepository.save(story);
    }

    @Transactional
    public void unlikeStory(Long storyId, Long userId) {
        likeRepository.findByUserIdAndStoryId(userId, storyId).ifPresent(like -> {
            likeRepository.delete(like);
            Story story = findById(storyId);
            story.setLikeCount(Math.max(0, story.getLikeCount() - 1));
            storyRepository.save(story);
        });
    }

    private StoryResponse enrichWithLikeStatus(StoryResponse response, Long userId) {
        if (userId != null) {
            response.setLikedByMe(likeRepository.existsByUserIdAndStoryId(userId, response.getId()));
        }
        return response;
    }

    private PagedResponse<StoryResponse> toPagedResponse(Page<Story> stories, Long userId) {
        return PagedResponse.<StoryResponse>builder()
                .content(stories.getContent().stream()
                        .map(s -> enrichWithLikeStatus(StoryResponse.from(s), userId))
                        .toList())
                .page(stories.getNumber())
                .size(stories.getSize())
                .totalElements(stories.getTotalElements())
                .totalPages(stories.getTotalPages())
                .last(stories.isLast())
                .build();
    }

    public Story findById(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + storyId));
    }
}
