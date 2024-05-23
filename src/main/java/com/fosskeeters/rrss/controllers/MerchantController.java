package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.models.Wish;
import com.fosskeeters.rrss.repositories.BrowsingHistoryRepository;
import com.fosskeeters.rrss.repositories.ProductRepository;
import com.fosskeeters.rrss.repositories.UserRepository;
import com.fosskeeters.rrss.repositories.WishRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final ProductRepository productRepository;

    public MerchantController(UserRepository userRepository,
                              BrowsingHistoryRepository browsingHistoryRepository,
                              WishRepository wishRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.browsingHistoryRepository = browsingHistoryRepository;
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
    }

    @GetMapping({"", "/"})
    public String getMerchantRedirectHandler(HttpSession session) {
        Optional<User> authenticatedUser =
                userRepository.findByUsername((String) session.getAttribute("username"));

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/merchants";
        }

        return "redirect:/merchants/" + authenticatedUser.get().getUsername();
    }

    @GetMapping("/{username}")
    public String getMerchantProducts(@RequestParam(required = false, defaultValue = "0") int page,
                                      HttpSession session, @PathVariable String username,
                                      Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/merchants/" + username;
        }

        Optional<User> authenticatedUser =
                userRepository.findByUsername((String) session.getAttribute("username"));
        Optional<User> targetUser = userRepository.findByUsername(username);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/merchants/" + username;
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

        Page<Product> userProducts =
                productRepository.findByOwner(targetUser.get(), PageRequest.of(page, 20));
        HashMap<Long, HashMap<String, Integer>> stats = new HashMap<>();

        for (Product product : userProducts) {
            HashMap<String, Integer> stat = new HashMap<>();

            int lastWeekViewCount = browsingHistoryRepository.findViewCountOfProduct(
                    product, LocalDateTime.now().minusDays(7), LocalDateTime.now());
            int lastMonthViewCount = browsingHistoryRepository.findViewCountOfProduct(
                    product, LocalDateTime.now().minusDays(30), LocalDateTime.now());
            int allViewCount = browsingHistoryRepository.findViewCountOfProduct(
                    product, product.getCreatedAt(), LocalDateTime.now());
            int wishCount = wishRepository.countByProduct(product);

            stat.put("lastWeekViewCount", lastWeekViewCount);
            stat.put("lastMonthViewCount", lastMonthViewCount);
            stat.put("allViewCount", allViewCount);
            stat.put("wishCount", wishCount);
            stats.put(product.getId(), stat);
        }

        model.addAttribute("stats", stats);
        model.addAttribute("products", userProducts);
        model.addAttribute("merchantUsername", username);
        return "merchants/products";
    }
}
