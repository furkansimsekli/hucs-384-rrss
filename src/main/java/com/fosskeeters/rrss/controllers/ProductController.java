package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.ReviewDto;
import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.Review;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.ProductRepository;
import com.fosskeeters.rrss.repositories.ReviewRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/products/")
public class ProductController {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository productRepository, ReviewRepository reviewRepository,
                             UserRepository userRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
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

    @PostMapping("/{productId}/reviews/create")
    public String createReview(HttpSession session, @PathVariable Long productId,
                               @Valid @ModelAttribute ReviewDto reviewDto,
                               BindingResult bindingResult, Model model) {
        Optional<Product> product = productRepository.findById(productId);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Check authentication
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            // Maybe we should throw 500, because being authenticated but not being in db is not OK
            // Need to handle the authentication/authorization somewhere else. It's repeating over
            // and over.
            return "redirect:/login";
        }

        // Only customers can do review
        if (user.get().getType() != User.Type.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Every customer can have at most one review
        if (reviewRepository.existsByProductIdAndAuthorId(productId, user.get().getId())) {
            model.addAttribute("notificationMessage", "You already reviewed this product!");
            return "redirect:/products/" + productId;
        }

        // Binding result check
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "redirect:/products/" + productId;
        }

        // Save new review to database
        Review review = new Review(reviewDto);
        review.setAuthor(user.get());
        review.setProduct(product.get());
        reviewRepository.save(review);
        model.addAttribute("notificationMessage", "Voila! Your review has been submitted.");
        return "redirect:/products/" + productId;
    }
}
