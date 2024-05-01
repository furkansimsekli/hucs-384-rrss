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
    public String getMerchantProducts(@PathVariable("username") String username,
                                      Model model) {

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("user", user.get());
        return "merchants/products";
    }

    @GetMapping("/{username}/products/create")
    public String getProductCreateForm(HttpSession session,
                                       @PathVariable("username") String username) {

        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return "merchants/create_product";
    }

    @PostMapping("/{username}/products/create")
    public String createProduct(HttpSession session,
                                @PathVariable("username") String username,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) {

        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
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
    public String getUpdateProductForm(HttpSession session,
                                       @PathVariable("username") String username,
                                       @PathVariable("product_id") long productId,
                                       Model model) {

        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Optional<Product> product = productRepository.findById(productId);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ProductDto productDto = new ProductDto(product.get());
        model.addAttribute("productDto", productDto);
        return "merchants/update_product";
    }

    @PostMapping("/{username}/products/{product_id}/update")
    public String updateProduct(HttpSession session,
                                @PathVariable("username") String username,
                                @PathVariable("product_id") long productId,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) {

        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            return "merchants/update_product";
        }

        Optional<Product> product = productRepository.findById(productId);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        product.get().setFromProductDto(productDto);
        productRepository.save(product.get());
        return "redirect:/merchants/" + username + "/products";
    }

    @GetMapping("/{username}/products/{product_id}/delete")
    public String deleteProduct(HttpSession session,
                                @PathVariable("username") String username,
                                @PathVariable("product_id") long productId) {

        if (!Objects.equals(username, session.getAttribute("username"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Optional<Product> product = productRepository.findById(productId);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        productRepository.delete(product.get());
        return "redirect:/merchants/" + username + "/products";
    }
}
