package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.Product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}
