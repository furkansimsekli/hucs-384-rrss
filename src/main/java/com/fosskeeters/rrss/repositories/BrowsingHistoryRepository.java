package com.fosskeeters.rrss.repositories;

import com.fosskeeters.rrss.models.BrowsingHistory;
import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {
    @Query("SELECT p FROM BrowsingHistory bh INNER JOIN Product p ON p = bh.product WHERE bh.viewedAt > :since GROUP BY p ORDER BY COUNT(p) DESC")
    public Page<Product> findMostViewedProduct(LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(bh) FROM BrowsingHistory bh WHERE :product = bh.product AND bh.viewedAt > :since")
    public long findViewCountOfProduct(Product product, LocalDateTime since);

    // clang-format off
    /* Query Explanation:
     *
     * SELECT p FROM Product p
     *  LEFT OUTER JOIN BrowsingHistory bh ON bh.product = p AND bh.owner = :customer             - 1
     *  INNER JOIN ProductKeyword pk ON pk.product = p                                            - 4
     *  JOIN (
     *    SELECT pk2.keyword as k2, count(keyword) AS kc2 FROM Product p2                         - 3
     *    INNER JOIN ProductKeyword pk2 ON p2 = pk2.product                                       - 2
     *    LEFT OUTER JOIN BrowsingHistory bh2 ON bh2.product = p2 WHERE bh2.owner = :customer     - 2
     *    GROUP BY pk2.keyword                                                                    - 3
     *  ) ON pk.keyword = k2                                                                      - 4
     *  WHERE bh.owner is null                                                                    - 1
     *  GROUP BY k2 ORDER BY kc2                                                                  - 4
     *  DESC LIMIT 10
     *
     *  1 - Match products with browsing history rows, select the ones that are not matched / Get products user has not visited before.
     *  2 - Match products with browsing history rows, select the ones that are visited by customer / Get products user has visited before.
     *  3 - Group and count keywords of matched products / Get a map of keywords:count for products user has visited before (2).
     *  4 - Match products with keyword rows, group and sort by keyword / Rank product results from (1) using keyword counts from (3)
     *
     *  This query is not guaranteed to return 10 results for some reason.
     *
     *  */
    // clang-format on
    @Query("SELECT p FROM Product p LEFT OUTER JOIN BrowsingHistory bh ON bh.product = p AND bh.owner = :customer INNER JOIN ProductKeyword pk ON pk.product = p JOIN (SELECT pk2.keyword as k2, count(keyword) AS kc2 FROM Product p2 INNER JOIN ProductKeyword pk2 ON p2 = pk2.product LEFT OUTER JOIN BrowsingHistory bh2 ON bh2.product = p2 WHERE bh2.owner = :customer GROUP BY pk2.keyword) ON pk.keyword = k2 WHERE bh.owner is null GROUP BY k2 ORDER BY kc2 DESC LIMIT 10")
    public List<Product> findRecommendedForUser(User customer);
}
