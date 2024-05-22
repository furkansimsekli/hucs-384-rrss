package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.BrowsingHistoryRepository;
import com.fosskeeters.rrss.repositories.ProductImageRepository;
import com.fosskeeters.rrss.repositories.ProductRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.data.domain.PageRequest;
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

        return "index";
    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam String query,
                                @RequestParam(required = false) String sort, Model model) {
        List<Product> results;

        if (sort != null && sort.equals("priceAsc")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByPriceAsc(
                                    query, query);
        } else if (sort != null && sort.equals("priceDesc")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByPriceDesc(
                                    query, query);
        } else if (sort != null && sort.equals("mostReviewed")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCase(query, query)
                            .stream()
                            .sorted((p1, p2)
                                            -> Integer.compare(p2.getReviews().size(),
                                                               p1.getReviews().size()))
                            .collect(Collectors.toList());
        } else if (sort != null && sort.equals("mostRecent")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCaseOrderByCreatedAtDesc(
                                    query, query);
        } else if (sort != null && sort.equals("bestScored")) {
            results =
                    productRepository
                            .findByNameContainingOrDescriptionContainingAllIgnoreCase(query, query)
                            .stream()
                            .sorted((p1, p2)
                                            -> Double.compare(p2.getAverageScore(),
                                                              p1.getAverageScore()))
                            .collect(Collectors.toList());
        } else {
            results = productRepository.findByNameContainingOrDescriptionContainingAllIgnoreCase(
                    query, query);
        }

        model.addAttribute("products", results);
        return "search";
    }
}
