package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<List<Product>> findTop10ByOrderByCreatedAtDesc();
}
