package com.trim.booking.repository;

import com.trim.booking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> { // JPA gives me free CRUD methods
    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER'")
    Long countActiveCustomers();

    boolean existsByEmail(String email);

    Page<User> findByRole(User.Role role, Pageable pageable);
}