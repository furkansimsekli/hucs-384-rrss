package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<List<Product>> findTop10ByOrderByCreatedAtDesc();
}
