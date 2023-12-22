package com.urcodebin.api.repository;

import com.urcodebin.api.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserAccount> findByUsername(String username);
}
