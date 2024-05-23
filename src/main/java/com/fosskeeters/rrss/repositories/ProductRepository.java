package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByOwner(User owner, Pageable pageable);

    List<Product> findTop10ByOrderByCreatedAtDesc();

    Page<Product> findByNameContainingOrDescriptionContainingAllIgnoreCase(String name,
                                                                           String description,
                                                                           Pageable pageable);

    Page<Product> findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByPriceAsc(
            String name, String description, Pageable pageable);

    Page<Product> findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByPriceDesc(
            String name, String description, Pageable pageable);

    Page<Product> findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByCreatedAtDesc(
            String name, String description, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN Review r ON r.product = p WHERE LOWER(p.name) LIKE '%' || LOWER(:name) || '%' OR LOWER(p.description) LIKE '%' || LOWER(:description) || '%' GROUP BY p ORDER BY COUNT(r) DESC")
    Page<Product> findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByReviewsSize(
            String name, String description, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN Review r ON r.product = p WHERE LOWER(p.name) LIKE '%' || LOWER(:name) || '%' OR LOWER(p.description) LIKE '%' || LOWER(:description) || '%' GROUP BY p ORDER BY AVG(r.score) DESC")
    Page<Product> findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByAverageScore(
            String name, String description, Pageable pageable);
}
