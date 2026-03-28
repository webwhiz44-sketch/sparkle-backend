package com.womensocial.app.service;

import com.womensocial.app.exception.BadRequestException;
import com.womensocial.app.model.dto.request.CreateReportRequest;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.dto.response.UserResponse;
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
public class ReportService {

    private final ReportRepository reportRepository;
    private final BlockRepository blockRepository;
    private final UserService userService;
    private final PostService postService;
    private final AnonymousPostService anonymousPostService;
    private final CommentService commentService;

    @Transactional
    public void createReport(Long reporterId, CreateReportRequest request) {
        if (request.getReportedUserId() == null
                && request.getPostId() == null
                && request.getAnonymousPostId() == null
                && request.getCommentId() == null) {
            throw new BadRequestException("At least one of: reportedUserId, postId, anonymousPostId, or commentId is required");
        }

        User reporter = userService.findUserById(reporterId);

        Report.ReportBuilder builder = Report.builder()
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription());

        if (request.getReportedUserId() != null) {
            builder.reportedUser(userService.findUserById(request.getReportedUserId()));
        }
        if (request.getPostId() != null) {
            builder.post(postService.findPostById(request.getPostId()));
        }
        if (request.getAnonymousPostId() != null) {
            builder.anonymousPost(anonymousPostService.findById(request.getAnonymousPostId()));
        }
        if (request.getCommentId() != null) {
            builder.comment(commentService.findById(request.getCommentId()));
        }

        reportRepository.save(builder.build());
    }

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BadRequestException("You cannot block yourself");
        }
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new BadRequestException("User is already blocked");
        }
        User blocker = userService.findUserById(blockerId);
        User blocked = userService.findUserById(blockedId);
        blockRepository.save(Block.builder().blocker(blocker).blocked(blocked).build());
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        Block block = blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .orElseThrow(() -> new BadRequestException("User is not blocked"));
        blockRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getBlockedUsers(Long userId, int page, int size) {
        Page<Block> blocks = blockRepository.findByBlockerId(userId,
                PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return PagedResponse.<UserResponse>builder()
                .content(blocks.getContent().stream()
                        .map(b -> UserResponse.from(b.getBlocked()))
                        .toList())
                .page(blocks.getNumber())
                .size(blocks.getSize())
                .totalElements(blocks.getTotalElements())
                .totalPages(blocks.getTotalPages())
                .last(blocks.isLast())
                .build();
    }
}
