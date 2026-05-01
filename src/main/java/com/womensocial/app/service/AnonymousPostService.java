package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.exception.UnauthorizedException;
import com.womensocial.app.model.dto.request.CreateAnonymousPostRequest;
import com.womensocial.app.model.dto.response.AnonymousPostResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.entity.AnonymousPost;
import com.womensocial.app.model.entity.Like;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.repository.AnonymousPostRepository;
import com.womensocial.app.repository.LikeRepository;
import com.womensocial.app.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnonymousPostService {

    private final AnonymousPostRepository anonymousPostRepository;
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final PollService pollService;

    @Transactional
    public AnonymousPostResponse createPost(Long userId, CreateAnonymousPostRequest request) {
        boolean hasContent = request.getContent() != null && !request.getContent().isBlank();
        boolean hasImage = request.getImageUrl() != null && !request.getImageUrl().isBlank();
        if (!hasContent && !hasImage) {
            throw new BadRequestException("Post must have content or an image");
        }

        User user = userService.findUserById(userId);

        AnonymousPost post = AnonymousPost.builder()
                .user(user)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .topicTags(request.getTopicTags())
                .build();

        post = anonymousPostRepository.save(post);

        if (request.getPoll() != null) {
            pollService.createPollForAnonymousPost(post, request.getPoll());
        }

        return AnonymousPostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AnonymousPostResponse> getFeed(Long userId, int page, int size) {
        Page<AnonymousPost> posts = anonymousPostRepository.findAllExcludingBlocked(userId,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return PagedResponse.<AnonymousPostResponse>builder()
                .content(posts.getContent().stream()
                        .map(p -> enrichWithLikeStatus(AnonymousPostResponse.from(p), userId))
                        .toList())
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .last(posts.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public AnonymousPostResponse getPost(Long postId, Long userId) {
        AnonymousPost post = findById(postId);
        return enrichWithLikeStatus(AnonymousPostResponse.from(post), userId);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        if (!anonymousPostRepository.existsByIdAndUserId(postId, userId)) {
            throw new UnauthorizedException("You can only delete your own posts");
        }
        anonymousPostRepository.deleteById(postId);
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        if (likeRepository.existsByUserIdAndAnonymousPostId(userId, postId)) {
            return;
        }
        AnonymousPost post = findById(postId);
        User user = userService.findUserById(userId);
        likeRepository.save(Like.builder().user(user).anonymousPost(post).build());
        post.setLikeCount(post.getLikeCount() + 1);
        anonymousPostRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        likeRepository.findByUserIdAndAnonymousPostId(userId, postId).ifPresent(like -> {
            likeRepository.delete(like);
            AnonymousPost post = findById(postId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            anonymousPostRepository.save(post);
        });
    }

    private AnonymousPostResponse enrichWithLikeStatus(AnonymousPostResponse response, Long userId) {
        if (userId != null) {
            response.setLikedByMe(likeRepository.existsByUserIdAndAnonymousPostId(userId, response.getId()));
        }
        return response;
    }

    public AnonymousPost findById(Long postId) {
        return anonymousPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Anonymous post not found with id: " + postId));
    }
}
