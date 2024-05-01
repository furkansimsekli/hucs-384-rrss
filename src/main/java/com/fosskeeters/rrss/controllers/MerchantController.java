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
    public String getProductCreateForm(HttpSession session, Model model,
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

        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Product product = new Product(user.get(), productDto);
        productRepository.save(product);

        return "redirect:/merchants/" + username + "/products";
    }

    @GetMapping("/{username}/products/{product_id}/update")
    public String getUpdateProductForm(HttpSession session, Model model, @PathVariable("username") String username,
                                       @PathVariable("product_id") long product_id) {
        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Optional<Product> product = productRepository.findById(product_id);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ProductDto productDto = new ProductDto(product.get());
        model.addAttribute("productDto", productDto);
        return "merchants/update_product";
    }

    @PostMapping("/{username}/products/{product_id}/update")
    public String updateProduct(HttpSession session, Model model, @PathVariable("username") String username,
                                @PathVariable("product_id") long product_id,
                                @Valid @ModelAttribute ProductDto productDto, BindingResult bindingResult) {
        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            return "merchants/update_product";
        }

        Optional<Product> product = productRepository.findById(product_id);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        product.get().setFromProductDto(productDto);
        productRepository.save(product.get());
        return "redirect:/merchants/" + username + "/products";
    }

    @GetMapping("/{username}/products/{product_id}/delete")
    public String deleteProduct(HttpSession session, Model model, @PathVariable("username") String username,
                                @PathVariable("product_id") long product_id) {
        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Optional<Product> product = productRepository.findById(product_id);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        productRepository.delete(product.get());
        return "redirect:/merchants/" + username + "/products";
    }
}
