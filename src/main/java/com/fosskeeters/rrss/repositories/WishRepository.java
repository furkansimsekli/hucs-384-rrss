package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.models.Wish;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {
    Optional<Wish> findByProductAndOwner(Product product, User owner);

    boolean existsByProductAndOwner(Product product, User owner);
}
