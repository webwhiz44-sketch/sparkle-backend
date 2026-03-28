package com.womensocial.app.service;

import com.womensocial.app.exception.ResourceNotFoundException;
import com.womensocial.app.exception.UnauthorizedException;
import com.womensocial.app.model.dto.request.CreateCommentRequest;
import com.womensocial.app.model.dto.response.CommentResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.entity.*;
import com.womensocial.app.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostService postService;
    private final AnonymousPostService anonymousPostService;
    private final LikeRepository likeRepository;

    @Transactional
    public CommentResponse commentOnPost(Long postId, Long userId, CreateCommentRequest request) {
        Post post = postService.findPostById(postId);
        User user = userService.findUserById(userId);
        Comment parent = resolveParent(request.getParentCommentId());

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .parentComment(parent)
                .content(request.getContent())
                .build();

        comment = commentRepository.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        // Post update handled lazily — count is denormalized
        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse commentOnAnonymousPost(Long postId, Long userId, CreateCommentRequest request) {
        AnonymousPost post = anonymousPostService.findById(postId);
        User user = userService.findUserById(userId);
        Comment parent = resolveParent(request.getParentCommentId());

        Comment comment = Comment.builder()
                .user(user)
                .anonymousPost(post)
                .parentComment(parent)
                .content(request.getContent())
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getPostComments(Long postId, Long userId, int page, int size) {
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.ASC, "createdAt")));
        return toPagedResponse(comments, userId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getAnonymousPostComments(Long postId, Long userId, int page, int size) {
        Page<Comment> comments = commentRepository.findByAnonymousPostIdAndParentCommentIsNull(postId,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.ASC, "createdAt")));
        return toPagedResponse(comments, userId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = findById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void likeComment(Long commentId, Long userId) {
        if (likeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            return;
        }
        Comment comment = findById(commentId);
        User user = userService.findUserById(userId);
        likeRepository.save(Like.builder().user(user).comment(comment).build());
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        likeRepository.findByUserIdAndCommentId(userId, commentId).ifPresent(like -> {
            likeRepository.delete(like);
            Comment comment = findById(commentId);
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentRepository.save(comment);
        });
    }

    private Comment resolveParent(Long parentId) {
        if (parentId == null) return null;
        return commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
    }

    private PagedResponse<CommentResponse> toPagedResponse(Page<Comment> comments, Long userId) {
        return PagedResponse.<CommentResponse>builder()
                .content(comments.getContent().stream()
                        .map(c -> {
                            CommentResponse r = CommentResponse.from(c);
                            if (userId != null) {
                                r.setLikedByMe(likeRepository.existsByUserIdAndCommentId(userId, c.getId()));
                            }
                            return r;
                        })
                        .toList())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .last(comments.isLast())
                .build();
    }

    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }
}
