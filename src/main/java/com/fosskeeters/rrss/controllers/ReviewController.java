package com.fosskeeters.rrss.controllers;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewController(ProductRepository productRepository, ReviewRepository reviewRepository,
                            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public String createReview(HttpSession session, @Valid @ModelAttribute ReviewDto reviewDto,
                               BindingResult bindingResult, Model model,
                               RedirectAttributes redirectAttrs) {
        long productId = reviewDto.getProductId();
        Optional<Product> product = productRepository.findById(productId);

        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/products/" + productId;
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/products/" + productId;
        }

        if (user.get().getType() != User.Type.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasFieldErrors("productId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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
            model.addAttribute("isProductOwner", false);
            model.addAttribute("reviewReplyDtoList", null);

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
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your review has been submitted.");
        return "redirect:/products/" + productId;
    }

    @PostMapping("/{reviewId}/update")
    public String updateReview(HttpSession session, @PathVariable long reviewId,
                               @Valid @ModelAttribute ReviewDto reviewDto,
                               BindingResult bindingResult, Model model,
                               RedirectAttributes redirectAttrs) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        long productId = review.get().getProduct().getId();

        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/products/" + productId;
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/products/" + productId;
        }

        // Don't let other users whose not the author herself or admin update the review
        if (review.get().getAuthor().getId() != user.get().getId()
            && user.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Product product = review.get().getProduct();

        // Binding result check
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            model.addAttribute("product", product);
            model.addAttribute("reviewId", review.get().getId());
            model.addAttribute("reviewDto", reviewDto);
            model.addAttribute("hasErrors", true);
            model.addAttribute("isCustomer", true);
            model.addAttribute("isProductOwner", false);
            model.addAttribute("reviewReplyDtoList", null);
            return "product";
        }

        // Save updated review to database
        review.get().setFromReviewDto(reviewDto);
        reviewRepository.save(review.get());
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your review has been updated!");
        return "redirect:/products/" + product.getId();
    }

    @GetMapping("/{reviewId}/delete")
    public String deleteReview(HttpSession session, @PathVariable long reviewId, Model model,
                               RedirectAttributes redirectAttrs) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        long productId = review.get().getProduct().getId();

        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/products/" + productId;
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/products/" + productId;
        }

        // Don't let other users whose not the author herself or admin delete the review
        if (review.get().getAuthor().getId() != user.get().getId()
            && user.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Delete the review from database
        reviewRepository.delete(review.get());
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Successfully deleted review.");
        return "redirect:/products/" + review.get().getProduct().getId();
    }

    @PostMapping("/{reviewId}/reply")
    public String replyReview(HttpSession session, @PathVariable long reviewId,
                              @Valid @ModelAttribute ReviewReplyDto reviewReplyDto,
                              BindingResult bindingResult, Model model) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        long productId = review.get().getProduct().getId();

        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/products/" + productId;
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/products/" + productId;
        }

        // Only one reply can be made
        if (review.get().getMerchantReplyBody() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Product product = review.get().getProduct();

        // Merchant must be the owner of the product in order to reply
        if (product.getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            List<ReviewReplyDto> reviewReplyDtoList = new ArrayList<>(
                    product.getReviews().stream().map(ReviewReplyDto::new).toList());

            model.addAttribute("product", product);
            model.addAttribute("reviewId", review.get().getId());
            model.addAttribute("reviewDto", null);
            model.addAttribute("hasErrors", true);
            model.addAttribute("isCustomer", false);
            model.addAttribute("isProductOwner", true);
            model.addAttribute("reviewReplyDtoList", reviewReplyDtoList);
            model.addAttribute("erroredReviewId", reviewId);
            return "product";
        }

        review.get().setFromReviewReplyDto(reviewReplyDto);
        reviewRepository.save(review.get());
        return "redirect:/products/" + product.getId();
    }

    @PostMapping("/{reviewId}/update-reply")
    public String updateReviewReply(HttpSession session, @PathVariable long reviewId,
                                    @Valid @ModelAttribute ReviewReplyDto reviewReplyDto,
                                    BindingResult bindingResult, Model model) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        long productId = review.get().getProduct().getId();

        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/products/" + productId;
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/products/" + productId;
        }

        Product product = review.get().getProduct();

        // Merchant must be the owner of the product in order to update the reply
        if (product.getOwner().getId() != user.get().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (review.get().getMerchantReplyBody() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            List<ReviewReplyDto> reviewReplyDtoList = new ArrayList<>(
                    product.getReviews().stream().map(ReviewReplyDto::new).toList());

            model.addAttribute("product", product);
            model.addAttribute("reviewId", review.get().getId());
            model.addAttribute("reviewDto", null);
            model.addAttribute("hasErrors", true);
            model.addAttribute("isCustomer", false);
            model.addAttribute("isProductOwner", true);
            model.addAttribute("reviewReplyDtoList", reviewReplyDtoList);
            model.addAttribute("erroredReviewId", reviewId);
            return "product";
        }

        review.get().setFromReviewReplyDto(reviewReplyDto);
        reviewRepository.save(review.get());
        return "redirect:/products/" + product.getId();
    }

    @GetMapping("/{reviewId}/delete-reply")
    public String deleteReviewReply(HttpSession session, @PathVariable long reviewId) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        long productId = review.get().getProduct().getId();

        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/products/" + productId;
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/products/" + productId;
        }

        User owner = review.get().getProduct().getOwner();

        // User must be the owner of the product in order to delete the reply or admin.
        if (owner.getId() != user.get().getId() && user.get().getType() != User.Type.ADMIN) {
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
