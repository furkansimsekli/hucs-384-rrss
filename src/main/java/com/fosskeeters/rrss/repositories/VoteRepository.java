package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Review;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.models.Vote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByReviewAndUser(Review review, Optional<User> user);
}
