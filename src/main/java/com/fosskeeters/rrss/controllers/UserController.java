package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.UserDto;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
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

    @GetMapping("/logout")
    public String logoutHandler(HttpSession session) {
        session.removeAttribute("username");
        return "redirect:/";
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
        Optional<User> user = userRepository.findByUsername(username.trim().toLowerCase());

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
    public String signupPostHandler(
            @Valid @ModelAttribute UserDto userDto, BindingResult bindingResult) {
        validateSignup(userDto, bindingResult);

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "signup";
        }

        User user = createUserFromDto(userDto);
        userRepository.save(user);
        return "redirect:/login";
    }

    private void validateSignup(UserDto userDto, BindingResult bindingResult) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            bindingResult.addError(
                    new FieldError("userDto", "username", "This username is already taken!"));
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            bindingResult.addError(
                    new FieldError("userDto", "email", "This email address is already taken!"));
        }

        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber())) {
            bindingResult.addError(
                    new FieldError(
                            "userDto", "phoneNumber", "This phone number is already taken!"));
        }

        if (!Objects.equals(userDto.getAccountType(), "customer")
                && !Objects.equals(userDto.getAccountType(), "merchant")) {
            bindingResult.addError(
                    new FieldError(
                            "userDto",
                            "accountType",
                            "Account type must be either Customer or Merchant!"));
        }

        if (!Objects.equals(userDto.getPassword1(), userDto.getPassword2())) {
            bindingResult.addError(
                    new FieldError("userDto", "password1", "Passwords do not match!"));
        }
    }

    private User createUserFromDto(UserDto userDto) {
        String encodedPassword = encoder.encode(userDto.getPassword1());
        int type = userDto.getAccountType().equals("customer") ? 3 : 2;

        return new User(
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getUsername(),
                encodedPassword,
                type,
                userDto.getEmail(),
                userDto.getPhoneNumber());
    }
}
