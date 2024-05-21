package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.repositories.ProductRepository;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {
    private final ProductRepository productRepository;

    public IndexController(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String indexGetController(Model model, HttpSession session){
        Optional<List<Product>> latestProducts = productRepository.findTop10ByOrderByCreatedAtDesc();

        if (session.getAttribute("username") == null) {
            // TODO : Use recommendation algorithm here.
        }

        model.addAttribute("latestProducts", latestProducts);

        return "index";
    }

}
