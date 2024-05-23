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
    public String getMerchantProducts(HttpSession session, @PathVariable String username,
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

        model.addAttribute("user", targetUser.get());
        return "merchants/products";
    }
}
