package com.trim.booking.repository;

import com.trim.booking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Business-filtered methods for multi-tenancy
    Optional<User> findByBusinessIdAndEmail(Long businessId, String email);

    Optional<User> findByIdAndBusinessId(Long id, Long businessId);

    Page<User> findByBusinessIdAndRole(Long businessId, User.Role role, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.business.id = :businessId AND u.role = 'CUSTOMER'")
    Long countByBusinessIdAndRole(@Param("businessId") Long businessId);

    boolean existsByBusinessIdAndEmail(Long businessId, String email);

    // Reset tokens are globally unique
    Optional<User> findByResetToken(String resetToken);

    // Global method for password reset - user not logged in, business context unknown
    Optional<User> findByEmail(String email);
}