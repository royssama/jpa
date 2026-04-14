package com.example.backend.repository.jpa;

import com.example.backend.domain.ComUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComUserRepository extends JpaRepository<ComUser, Long> {

    Optional<ComUser> findByUserId(String userId);
}
