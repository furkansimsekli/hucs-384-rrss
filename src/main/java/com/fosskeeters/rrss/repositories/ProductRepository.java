package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findTop10ByOrderByCreatedAtDesc();

    List<Product> findByNameContainingOrDescriptionContainingAllIgnoreCase(String name,
                                                                           String description);
}
