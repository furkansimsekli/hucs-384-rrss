package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.BrowsingHistoryRepository;
import com.fosskeeters.rrss.repositories.ProductRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

@Controller
public class MiscController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BrowsingHistoryRepository browsingHistoryRepository;

    public MiscController(ProductRepository productRepository, UserRepository userRepository,
                          BrowsingHistoryRepository browsingHistoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.browsingHistoryRepository = browsingHistoryRepository;
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
}
