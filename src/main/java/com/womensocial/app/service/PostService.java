package com.womensocial.app.service;

import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.exception.UnauthorizedException;
import com.womensocial.app.model.dto.request.CreatePostRequest;
import com.womensocial.app.model.dto.request.UpdatePostRequest;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.dto.response.PostResponse;
import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.model.entity.*;
import com.womensocial.app.repository.*;
import com.womensocial.app.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final CommunityRepository communityRepository;
    private final LikeRepository likeRepository;
    private final SavedPostRepository savedPostRepository;
    private final PollService pollService;

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        User user = userService.findUserById(userId);
        Community community = null;

        if (request.getCommunityId() != null) {
            community = communityRepository.findById(request.getCommunityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community not found"));
        }

        Post post = Post.builder()
                .user(user)
                .community(community)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .topicTags(request.getTopicTags())
                .build();

        post = postRepository.save(post);

        if (community != null) {
            community.setPostCount(community.getPostCount() + 1);
            communityRepository.save(community);
        }

        if (request.getPoll() != null) {
            pollService.createPollForPost(post, request.getPoll());
        }

        return enrichWithLikeStatus(PostResponse.from(post), userId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getFeed(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = userId != null
                ? postRepository.findFeedForUser(userId, pageRequest)
                : postRepository.findAll(pageRequest);
        return toPagedResponse(posts, userId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getPostsByTag(String tag, Long userId, int page, int size) {
        Page<Post> posts = postRepository.findByTag(tag,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE)));
        return toPagedResponse(posts, userId);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId, Long currentUserId) {
        Post post = findPostById(postId);
        PostResponse response = PostResponse.from(post);
        return enrichWithLikeStatus(response, currentUserId);
    }

    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = findPostById(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own posts");
        }
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setTopicTags(request.getTopicTags());
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = findPostById(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own posts");
        }
        if (post.getCommunity() != null) {
            Community community = post.getCommunity();
            community.setPostCount(Math.max(0, community.getPostCount() - 1));
            communityRepository.save(community);
        }
        postRepository.delete(post);
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            return;
        }
        Post post = findPostById(postId);
        User user = userService.findUserById(userId);
        likeRepository.save(Like.builder().user(user).post(post).build());
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        likeRepository.findByUserIdAndPostId(userId, postId).ifPresent(like -> {
            likeRepository.delete(like);
            Post post = findPostById(postId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postRepository.save(post);
        });
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getCommunityPosts(Long communityId, Long userId, int page, int size) {
        Page<Post> posts = postRepository.findByCommunityId(communityId,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        return toPagedResponse(posts, userId);
    }

    @Transactional
    public void savePost(Long postId, Long userId) {
        if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BadRequestException("Post already saved");
        }
        Post post = findPostById(postId);
        User user = userService.findUserById(userId);
        savedPostRepository.save(SavedPost.builder().user(user).post(post).build());
    }

    @Transactional
    public void unsavePost(Long postId, Long userId) {
        savedPostRepository.findByUserIdAndPostId(userId, postId)
                .ifPresent(savedPostRepository::delete);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getSavedPosts(Long userId, int page, int size) {
        Page<SavedPost> saved = savedPostRepository.findByUserId(userId,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE)));
        return PagedResponse.<PostResponse>builder()
                .content(saved.getContent().stream()
                        .map(sp -> {
                            PostResponse r = PostResponse.from(sp.getPost());
                            r.setSavedByMe(true);
                            r.setLikedByMe(likeRepository.existsByUserIdAndPostId(userId, sp.getPost().getId()));
                            return r;
                        })
                        .toList())
                .page(saved.getNumber())
                .size(saved.getSize())
                .totalElements(saved.getTotalElements())
                .totalPages(saved.getTotalPages())
                .last(saved.isLast())
                .build();
    }

    private PostResponse enrichWithLikeStatus(PostResponse response, Long userId) {
        if (userId != null) {
            response.setLikedByMe(likeRepository.existsByUserIdAndPostId(userId, response.getId()));
            response.setSavedByMe(savedPostRepository.existsByUserIdAndPostId(userId, response.getId()));
        }
        return response;
    }

    private PagedResponse<PostResponse> toPagedResponse(Page<Post> posts, Long userId) {
        return PagedResponse.<PostResponse>builder()
                .content(posts.getContent().stream()
                        .map(p -> enrichWithLikeStatus(PostResponse.from(p), userId))
                        .toList())
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .last(posts.isLast())
                .build();
    }

    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }
}
