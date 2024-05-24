package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.BrowsingHistoryRepository;
import com.fosskeeters.rrss.repositories.ProductImageRepository;
import com.fosskeeters.rrss.repositories.ProductRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

@Controller
public class MiscController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BrowsingHistoryRepository browsingHistoryRepository;
    private final ProductImageRepository productImageRepository;

    public MiscController(ProductRepository productRepository, UserRepository userRepository,
                          BrowsingHistoryRepository browsingHistoryRepository,
                          ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.browsingHistoryRepository = browsingHistoryRepository;
        this.productImageRepository = productImageRepository;
    }

    @GetMapping("/")
    public String indexGetController(Model model, HttpSession session) {
        model.addAttribute("trendingProducts",
                           browsingHistoryRepository.findMostViewedProduct(
                                   LocalDateTime.now().minusDays(7), PageRequest.of(0, 10)));
        model.addAttribute("latestProducts", productRepository.findTop10ByOrderByCreatedAtDesc());

        if (session.getAttribute("username") != null) {
            Optional<User> user =
                    userRepository.findByUsername((String) session.getAttribute("username"));

            if (user.isEmpty()) {
                session.removeAttribute("username");
                return "redirect:/login";
            }

            model.addAttribute("recommendations",
                               browsingHistoryRepository.findRecommendedForUser(user.get()));
        } else {
            model.addAttribute("recommendations",
                               browsingHistoryRepository.findMostViewedProduct(
                                       LocalDateTime.now().minusDays(7), PageRequest.of(0, 30)));
        }

        model.addAttribute("title", "Home - ShopSmart");
        return "index";
    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam String query,
                                @RequestParam(required = false) String sort,
                                @RequestParam(required = false, defaultValue = "0") int page,
                                Model model) {
        Page<Product> results;

        if (sort != null && sort.equals("priceAsc")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByPriceAsc(
                                    query, query, PageRequest.of(page, 20));
        } else if (sort != null && sort.equals("priceDesc")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByPriceDesc(
                                    query, query, PageRequest.of(page, 20));
        } else if (sort != null && sort.equals("mostReviewed")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByReviewsSize(
                                    query, query, PageRequest.of(page, 20));
        } else if (sort != null && sort.equals("mostRecent")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByCreatedAtDesc(
                                    query, query, PageRequest.of(page, 20));
        } else if (sort != null && sort.equals("bestScored")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByAverageScore(
                                    query, query, PageRequest.of(page, 20));
        } else {
            results = productRepository.findByNameContainingOrDescriptionContainingAllIgnoreCase(
                    query, query, PageRequest.of(page, 20));
        }

        model.addAttribute("products", results);
        model.addAttribute("title", "Search - ShopSmart");

        return "search";
    }
}
