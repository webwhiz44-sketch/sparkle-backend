package com.womensocial.app.service;

import com.womensocial.app.model.dto.response.NotificationResponse;
import com.womensocial.app.model.dto.response.PagedResponse;
import com.womensocial.app.model.entity.Notification;
import com.womensocial.app.model.entity.User;
import com.womensocial.app.model.enums.NotificationType;
import com.womensocial.app.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        List<NotificationResponse> content = notifications.getContent()
                .stream()
                .map(NotificationResponse::from)
                .toList();
        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .last(notifications.isLast())
                .build();
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void send(User recipient, User actor, NotificationType type, String message,
                     Long postId, Long communityId) {
        // Never notify yourself
        if (recipient.getId().equals(actor.getId())) return;

        Notification notification = Notification.builder()
                .user(recipient)
                .actor(actor)
                .type(type)
                .message(message)
                .postId(postId)
                .communityId(communityId)
                .build();
        notificationRepository.save(notification);
    }
}
