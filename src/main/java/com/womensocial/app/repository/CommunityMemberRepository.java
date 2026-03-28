package com.womensocial.app.repository;

import com.womensocial.app.model.entity.CommunityMember;
import com.womensocial.app.model.enums.MemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);
    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);
    Page<CommunityMember> findByCommunityId(Long communityId, Pageable pageable);
    long countByCommunityId(Long communityId);
    boolean existsByCommunityIdAndUserIdAndRole(Long communityId, Long userId, MemberRole role);
}
