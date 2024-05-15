package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/signup-requests")
    public String signupRequests(HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (user.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<User> awaitingUserList = userRepository.findAllByIsApproved(false);
        model.addAttribute("awaitingUserList", awaitingUserList);
        return "admin/signup_requests";
    }

    @GetMapping("/{username}/approve")
    public String approveSignupRequest(@PathVariable String username, HttpSession session) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String authenticatedUsername = session.getAttribute("username").toString();
        Optional<User> authenticatedUser = userRepository.findByUsername(authenticatedUsername);
        Optional<User> awaitingUser = userRepository.findByUsername(username);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (awaitingUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (authenticatedUser.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        awaitingUser.get().setApproved(true);
        userRepository.save(awaitingUser.get());
        return "redirect:/admin/signup-requests";
    }

    @GetMapping("/{username}/reject")
    public String rejectSignupRequest(@PathVariable String username, HttpSession session) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String authenticatedUsername = session.getAttribute("username").toString();
        Optional<User> authenticatedUser = userRepository.findByUsername(authenticatedUsername);
        Optional<User> awaitingUser = userRepository.findByUsername(username);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (awaitingUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (authenticatedUser.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        userRepository.delete(awaitingUser.get());
        return "redirect:/admin/signup-requests";
    }
}
