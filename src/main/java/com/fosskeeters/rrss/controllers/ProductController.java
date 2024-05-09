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

import java.util.Objects;
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
    public String productGetHandler(HttpSession session, @PathVariable Long id, Model model) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        User user = null;
        Review review = null;
        if (session.getAttribute("username") != null) {
            review = product.get()
                             .getReviews()
                             .stream()
                             .filter(r
                                     -> r.getAuthor().getUsername().equals(
                                             session.getAttribute("username")))
                             .findFirst()
                             .orElse(null);

            Optional<User> userOpt =
                    userRepository.findByUsername((String) session.getAttribute("username"));
            user = userOpt.isPresent() ? userOpt.get() : null;
        }
        model.addAttribute("isCustomer",
                           user != null ? user.getType() == User.Type.CUSTOMER : false);
        model.addAttribute("product", product.get());
        model.addAttribute("reviewId", review != null ? review.getId() : null);
        model.addAttribute("reviewDto", review != null ? new ReviewDto(review) : new ReviewDto());
        model.addAttribute("hasErrors", false);
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
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // Binding result check
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", product.get());
            model.addAttribute("reviewId", null);
            model.addAttribute("reviewDto", reviewDto);
            model.addAttribute("hasErrors", true);

            // This should not be needed as we're returning FORBIDDEN for non-customer users above
            // but in a real codebase this would be a trap waiting for someone to move the check
            // above to somewhere else. Assert to condition to make it explicit what we're depending
            // on.
            assert user.get().getType() == User.Type.CUSTOMER;
            model.addAttribute("isCustomer", true);

            return "product";
        }

        // Save new review to database
        Review review = new Review(reviewDto);
        review.setAuthor(user.get());
        review.setProduct(product.get());
        reviewRepository.save(review);
        model.addAttribute("notificationMessage", "Voila! Your review has been submitted.");
        return "redirect:/products/" + productId;
    }

    @PostMapping("/{productId}/reviews/{reviewId}/update")
    public String updateReview(HttpSession session, @PathVariable Long productId,
                               @PathVariable Long reviewId,
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

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Don't let other users whose not the author herself update the review
        if (!Objects.equals(review.get().getAuthor().getId(), user.get().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Binding result check
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            model.addAttribute("product", product.get());
            model.addAttribute("reviewId", review.get().getId());
            model.addAttribute("reviewDto", reviewDto);
            model.addAttribute("hasErrors", true);
            model.addAttribute("isCustomer", true);
            return "product";
        }

        // Save updated review to database
        review.get().setFromReviewDto(reviewDto);
        reviewRepository.save(review.get());
        model.addAttribute("notificationMessage", "Voila! Your review has been updated!");
        return "redirect:/products/" + productId;
    }

    @GetMapping("/{productId}/reviews/{reviewId}/delete")
    public String deleteReview(HttpSession session, @PathVariable Long productId,
                               @PathVariable Long reviewId, Model model) {
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

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Don't let other users whose not the author herself delete the review
        if (!Objects.equals(review.get().getAuthor().getId(), user.get().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Delete the review from database
        reviewRepository.delete(review.get());
        model.addAttribute("notificationMessage", "Oh no! Where did your review go?");
        return "redirect:/products/" + productId;
    }
}
