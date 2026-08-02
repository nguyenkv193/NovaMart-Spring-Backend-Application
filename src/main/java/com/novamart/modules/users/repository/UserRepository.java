package com.novamart.modules.users.repository;

import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
