package com.womensocial.app.repository;

import com.womensocial.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("""
            SELECT u FROM User u
            WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY u.displayName ASC
            """)
    java.util.List<User> searchByDisplayName(@org.springframework.data.repository.query.Param("query") String query,
                                              org.springframework.data.domain.Pageable pageable);
}
