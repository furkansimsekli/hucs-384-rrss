package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.BrowsingHistory;
import com.fosskeeters.rrss.models.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {
    @Query("SELECT p FROM BrowsingHistory bh INNER JOIN Product p ON p = bh.product WHERE bh.viewedAt > :since GROUP BY p ORDER BY COUNT(p) DESC")
    public Page<Product> findMostViewedProduct(LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(bh) FROM BrowsingHistory bh WHERE :product = bh.product AND bh.viewedAt > :since")
    public long findViewCountOfProduct(Product product, LocalDateTime since);
}
