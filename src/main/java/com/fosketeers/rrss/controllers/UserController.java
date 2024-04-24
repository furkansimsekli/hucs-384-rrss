package com.fosketeers.rrss.controllers;

import com.fosketeers.rrss.models.User;
import com.fosketeers.rrss.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class UserController {
    private UserRepository userRepository;
    private Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{id}")
    public String userProfileHandler(Model model, @PathVariable String id) {
        return "user";
    }

    @GetMapping("/login")
    public String loginGetHandler(Model model, HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/";
        }

        return "login";
    }

    @PostMapping("/login")
    public String loginPostHandler(
            @RequestParam String username, @RequestParam String password, HttpSession session) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            if (encoder.matches(password, user.get().getPassword())) {
                session.setAttribute("username", user.get().getUsername());
                return "redirect:/";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signupGetHandler(Model model, HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/";
        }

        return "signup";
    }

    @PostMapping("/signup")
    public String signupPostHandler(@RequestParam String username, @RequestParam String password1) {
        User user = new User();

        user.setUsername(username);
        user.setPassword(encoder.encode(password1));
        userRepository.save(user);

        return "redirect:/login";
    }
}
