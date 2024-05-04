package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.UserDto;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {
    private UserRepository userRepository;
    private Argon2PasswordEncoder encoder;

    public AuthenticationController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @GetMapping("/signup")
    public String signupGetHandler(HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/";
        }

        return "signup";
    }

    @PostMapping("/signup")
    public String signupPostHandler(@Valid @ModelAttribute UserDto userDto,
                                    BindingResult bindingResult) {
        validateSignup(userDto, bindingResult);

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "signup";
        }

        String encodedPassword = encoder.encode(userDto.getPassword1());
        User user = new User(userDto, encodedPassword);
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginGetHandler(HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/";
        }

        return "login";
    }

    @PostMapping("/login")
    public String loginPostHandler(@RequestParam String username, @RequestParam String password,
                                   HttpSession session) {
        Optional<User> user = userRepository.findByUsername(username.trim().toLowerCase());

        if (user.isPresent()) {
            if (encoder.matches(password, user.get().getPassword())) {
                session.setAttribute("username", user.get().getUsername());
                return "redirect:/";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logoutHandler(HttpSession session) {
        session.removeAttribute("username");
        return "redirect:/";
    }

    /**
     * Validates the data provided in a UserDto object for user registration.
     * This method checks for the following conflicts and adds error messages to the provided
     * BindingResult object if any are found:
     * <p>
     * 1. Username must not exist in the system.
     * <p>
     * 2. Email address must not exist in the system.
     * <p>
     * 3. Phone number must not exist in the system.
     * <p>
     * 4. Account type must be either "customer" or "merchant".
     * <p>
     * 5. Password and re-type password must match with each other.
     *
     * @param userDto The UserDto object containing the user registration information.
     * @param bindingResult The BindingResult object to which validation errors will be added.
     */
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
            bindingResult.addError(new FieldError("userDto", "phoneNumber",
                                                  "This phone number is already taken!"));
        }

        if (!Objects.equals(userDto.getAccountType(), "customer")
            && !Objects.equals(userDto.getAccountType(), "merchant")) {
            bindingResult.addError(new FieldError(
                    "userDto", "accountType", "Account type must be either Customer or Merchant!"));
        }

        if (!Objects.equals(userDto.getPassword1(), userDto.getPassword2())) {
            bindingResult.addError(
                    new FieldError("userDto", "password1", "Passwords do not match!"));
        }
    }
}
