package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.repositories.ProductRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.servlet.http.HttpSession;

@Controller
public class MiscController {
    private final ProductRepository productRepository;

    public MiscController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String indexGetController(Model model, HttpSession session) {
        // TODO: get trending products from browsing history (last 24h maybe)
        List<Product> trendingProducts = productRepository.findTop10ByOrderByCreatedAtDesc();
        List<Product> latestProducts = productRepository.findTop10ByOrderByCreatedAtDesc();

        if (session.getAttribute("username") != null) {
            // TODO : Use recommendation algorithm here.
        }

        model.addAttribute("carouselProducts", trendingProducts);
        model.addAttribute("latestProducts", latestProducts);

        return "index";
    }
}
