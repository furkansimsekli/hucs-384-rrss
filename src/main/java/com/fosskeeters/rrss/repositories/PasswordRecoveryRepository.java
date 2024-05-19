package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.PasswordRecovery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, Long> {
    Optional<PasswordRecovery> findByToken(String token);

    List<PasswordRecovery> findAllByIsEmailSentIsFalseOrderByCreatedAtAsc();
}
