package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByProductIdAndAuthorId(Long productId, Long authorId);
}
