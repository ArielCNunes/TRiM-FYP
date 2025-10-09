package com.trim.booking.repository;

import com.trim.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> { // JPA gives me free CRUD methods
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}