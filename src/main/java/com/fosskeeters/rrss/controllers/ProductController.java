package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.ProductDto;
import com.fosskeeters.rrss.dtos.ReviewDto;
import com.fosskeeters.rrss.dtos.ReviewReplyDto;
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

import java.util.ArrayList;
import java.util.List;
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
    public String productGetHandler(HttpSession session, @PathVariable long id, Model model) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        User user = null;
        Review review = null;
        List<ReviewReplyDto> reviewDtoList = new ArrayList<>();
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

            if (user != null && product.get().getOwner().getId() == user.getId()) {
                reviewDtoList.addAll(
                        product.get().getReviews().stream().map(ReviewReplyDto::new).toList());
            }
        }
        model.addAttribute("isCustomer",
                           user != null ? user.getType() == User.Type.CUSTOMER : false);
        model.addAttribute("isProductOwner",
                           user != null ? user.getId() == product.get().getOwner().getId() : false);
        model.addAttribute("reviewReplyDtoList", !reviewDtoList.isEmpty() ? reviewDtoList : null);
        model.addAttribute("product", product.get());
        model.addAttribute("reviewId", review != null ? review.getId() : null);
        model.addAttribute("reviewDto", review != null ? new ReviewDto(review) : new ReviewDto());
        model.addAttribute("hasErrors", false);
        return "product";
    }

    @GetMapping("/create")
    public String getProductCreateForm(HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        Optional<User> user =
                userRepository.findByUsername(session.getAttribute("username").toString());

        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("productDto", new ProductDto());
        return "merchants/create_product";
    }

    @PostMapping("/create")
    public String createProduct(HttpSession session, @Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "merchants/create_product";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Product product = new Product(user.get(), productDto);
        productRepository.save(product);
        return "redirect:/merchants/" + username;
    }

    @GetMapping("/{productId}/update")
    public String getUpdateProductForm(HttpSession session, @PathVariable long productId,
                                       Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        Optional<User> user =
                userRepository.findByUsername(session.getAttribute("username").toString());
        Optional<Product> product = productRepository.findById(productId);

        if (user.isEmpty() || product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (product.get().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ProductDto productDto = new ProductDto(product.get());
        model.addAttribute("productDto", productDto);
        return "merchants/update_product";
    }

    @PostMapping("/{productId}/update")
    public String updateProduct(HttpSession session, @PathVariable long productId,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "merchants/update_product";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Product> product = productRepository.findById(productId);

        if (user.isEmpty() || product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (product.get().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        product.get().setFromProductDto(productDto);
        productRepository.save(product.get());
        return "redirect:/merchants/" + username;
    }

    @GetMapping("/{productId}/delete")
    public String deleteProduct(HttpSession session, @PathVariable long productId) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Product> product = productRepository.findById(productId);

        if (user.isEmpty() || product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (product.get().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        productRepository.delete(product.get());
        return "redirect:/merchants/" + username;
    }

    @PostMapping("/{productId}/reviews/create")
    public String createReview(HttpSession session, @PathVariable long productId,
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
    public String updateReview(HttpSession session, @PathVariable long productId,
                               @PathVariable long reviewId,
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
        if (review.get().getAuthor().getId() != user.get().getId()) {
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
    public String deleteReview(HttpSession session, @PathVariable long productId,
                               @PathVariable long reviewId, Model model) {
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
        if (review.get().getAuthor().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Delete the review from database
        reviewRepository.delete(review.get());
        model.addAttribute("notificationMessage", "Oh no! Where did your review go?");
        return "redirect:/products/" + productId;
    }

    // Different endpoint structure is started to being used.
    // Instead of changing old ones, I'm writing this one with the new structure
    // TODO: delete these commands after adapting other handlers as well
    @PostMapping("/reviews/{reviewId}/reply")
    public String replyReview(HttpSession session, @PathVariable long reviewId,
                              @Valid @ModelAttribute ReviewReplyDto reviewReplyDto,
                              BindingResult bindingResult) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Review> review = reviewRepository.findById(reviewId);

        if (user.isEmpty() || review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Only one reply can be made
        if (review.get().getMerchantReplyBody() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // Merchant must be the owner of the product in order to reply
        if (review.get().getProduct().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "product";
        }

        review.get().setFromReviewReplyDto(reviewReplyDto);
        reviewRepository.save(review.get());
        return "redirect:/products/" + review.get().getProduct().getId();
    }

    // Different endpoint structure is started to being used.
    // Instead of changing old ones, I'm writing this one with the new structure
    // TODO: delete these commands after adapting other handlers as well
    @PostMapping("/reviews/{reviewId}/update-reply")
    public String updateReviewReply(HttpSession session, @PathVariable long reviewId,
                                    @Valid @ModelAttribute ReviewReplyDto reviewReplyDto,
                                    BindingResult bindingResult) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Review> review = reviewRepository.findById(reviewId);

        if (user.isEmpty() || review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Merchant must be the owner of the product in order to update the reply
        if (review.get().getProduct().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (review.get().getMerchantReplyBody() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "product";
        }

        review.get().setFromReviewReplyDto(reviewReplyDto);
        reviewRepository.save(review.get());
        return "redirect:/products/" + review.get().getProduct().getId();
    }

    // Different endpoint structure is started to being used.
    // Instead of changing old ones, I'm writing this one with the new structure
    // TODO: delete these commands after adapting other handlers as well
    @PostMapping("/reviews/{reviewId}/delete-reply")
    public String deleteReviewReply(HttpSession session, @PathVariable long reviewId) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Review> review = reviewRepository.findById(reviewId);

        if (user.isEmpty() || review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Merchant must be the owner of the product in order to delete the reply
        if (review.get().getProduct().getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (review.get().getMerchantReplyBody() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        review.get().setMerchantReplyBody(null);
        reviewRepository.save(review.get());
        return "redirect:/products/" + review.get().getProduct().getId();
    }
}
