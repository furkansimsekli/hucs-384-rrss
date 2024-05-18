package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.ProductImage;

import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.transaction.Transactional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @Transactional
    void deleteAllByProduct(Product product);
}
