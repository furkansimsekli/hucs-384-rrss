package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.models.Wish;
import com.fosskeeters.rrss.repositories.BrowsingHistoryRepository;
import com.fosskeeters.rrss.repositories.UserRepository;
import com.fosskeeters.rrss.repositories.WishRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchants")
public class MerchantController {
    private final UserRepository userRepository;
    private final BrowsingHistoryRepository browsingHistoryRepository;
    private final WishRepository wishRepository;

    public MerchantController(UserRepository userRepository,
                              BrowsingHistoryRepository browsingHistoryRepository,
                              WishRepository wishRepository) {
        this.userRepository = userRepository;
        this.browsingHistoryRepository = browsingHistoryRepository;
        this.wishRepository = wishRepository;
    }

    @GetMapping("/{username}")
    public String getMerchantProducts(HttpSession session, @PathVariable String username,
                                      Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        Optional<User> authenticatedUser =
                userRepository.findByUsername((String) session.getAttribute("username"));
        Optional<User> targetUser = userRepository.findByUsername(username);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (targetUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!Objects.equals(username, session.getAttribute("username"))
            && authenticatedUser.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (targetUser.get().getType() != User.Type.MERCHANT) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<Product> userProducts = targetUser.get().getProducts();
        for (Product product : userProducts) {
            product.setViewsLastWeek(browsingHistoryRepository.findViewCountOfProduct(
                    product, LocalDateTime.now().minusDays(7), LocalDateTime.now()));
            product.setViewsLastMonth(browsingHistoryRepository.findViewCountOfProduct(
                    product, LocalDateTime.now().minusDays(30), LocalDateTime.now()));
            product.setAllViews(browsingHistoryRepository.findViewCountOfProduct(
                    product, product.getCreatedAt(), LocalDateTime.now()));
            product.setWishCount(wishRepository.countByProduct(product));
        }
        model.addAttribute("products", userProducts);
        return "merchants/products";
    }
}
