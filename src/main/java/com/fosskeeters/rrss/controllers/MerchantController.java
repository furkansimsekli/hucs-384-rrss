package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.ProductDto;
import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.ProductRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/merchants")
public class MerchantController {
    private UserRepository userRepository;
    private ProductRepository productRepository;

    public MerchantController(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/{username}/products")
    public String getMerchantProducts(Model model, @PathVariable("username") String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            model.addAttribute("user", user.get());
        }

        return "merchants/products";
    }

    @GetMapping("/{username}/products/create")
    public String getMerchantProductCreateForm(HttpSession session, Model model,
                                               @PathVariable("username") String username) {
        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return "merchants/create_product";
    }

    @PostMapping("/{username}/products/create")
    public String createProduct(HttpSession session, Model model, @PathVariable("username") String username,
                                @Valid @ModelAttribute ProductDto productDto, BindingResult bindingResult) {
        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", true);
            System.out.println(bindingResult);
            return "merchants/create_product";
        }

        Optional<User> user = userRepository.findByUsername(username);

        Product product = new Product();
        product.setOwner(user.get());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCreatedAt(LocalDateTime.now());
        productRepository.save(product);

        return "redirect:/merchants/" + username + "/products";
    }
}
