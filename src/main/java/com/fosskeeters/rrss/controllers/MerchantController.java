package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchants")
public class MerchantController {
    private final UserRepository userRepository;

    public MerchantController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{username}")
    public String getMerchantProducts(HttpSession session, @PathVariable String username,
                                      Model model) {
        Optional<User> user = userRepository.findByUsername(username);

        if (!Objects.equals(username, session.getAttribute("username")) || user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("user", user.get());
        return "merchants/products";
    }
}
