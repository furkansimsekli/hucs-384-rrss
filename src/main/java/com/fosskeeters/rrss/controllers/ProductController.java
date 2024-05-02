package com.fosskeeters.rrss.controllers;

import java.util.Optional;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.repositories.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/products/")
public class ProductController {
    private ProductRepository productRepository;

    public ProductController(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @GetMapping("/{id}")
    public String productGetHandler(@PathVariable Long id, Model model) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("product", product.get());
        return "product";
    }
}
