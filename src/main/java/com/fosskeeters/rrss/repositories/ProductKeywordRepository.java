package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.ProductKeyword;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import jakarta.transaction.Transactional;

public interface ProductKeywordRepository extends JpaRepository<ProductKeyword, Long> {
    @Transactional
    void deleteAllByProduct(Product product);

    List<ProductKeyword> findAllByProduct(Product product);
}
