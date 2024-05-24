package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.PasswordRecoveryDto;
import com.fosskeeters.rrss.dtos.UserDto;
import com.fosskeeters.rrss.models.PasswordRecovery;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.PasswordRecoveryRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {
    private final UserRepository userRepository;
    private final Argon2PasswordEncoder encoder;
    private final PasswordRecoveryRepository passwordRecoveryRepository;

    public AuthenticationController(UserRepository userRepository,
                                    PasswordRecoveryRepository passwordRecoveryRepository) {
        this.userRepository = userRepository;
        this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        this.passwordRecoveryRepository = passwordRecoveryRepository;
    }

    @GetMapping("/signup")
    public String signupGetHandler(HttpSession session, Model model) {
        model.addAttribute("title", "Sign Up - ShopSmart");

        if (session.getAttribute("username") != null) {
            return "redirect:/";
        }

        model.addAttribute("userDto", new UserDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupPostHandler(@Valid @ModelAttribute UserDto userDto,
                                    BindingResult bindingResult, Model model,
                                    RedirectAttributes redirectAttrs) {
        model.addAttribute("title", "Sign Up - ShopSmart");

        validateSignup(userDto, bindingResult);
        model.addAttribute("userDto", userDto);

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "signup";
        }

        String encodedPassword = encoder.encode(userDto.getPassword1());
        User user = new User(userDto, encodedPassword);
        userRepository.save(user);
        redirectAttrs.addFlashAttribute(
                "notification",
                "success:Sign up request has been made, please wait for the approval and thanks for the patience in advance!");
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginGetHandler(HttpSession session, Model model) {
        model.addAttribute("title", "Log In - ShopSmart");

        if (session.getAttribute("username") != null) {
            return "redirect:/";
        }

        model.addAttribute("error", false);
        return "login";
    }

    @PostMapping("/login")
    public String loginPostHandler(@RequestParam String username, @RequestParam String password,
                                   @RequestParam(required = false) String next, HttpSession session,
                                   Model model, RedirectAttributes redirectAttrs) {
        model.addAttribute("title", "Log In - ShopSmart");

        Optional<User> user = userRepository.findByUsername(username.trim().toLowerCase());

        if (user.isPresent()) {
            if (!user.get().isApproved()) {
                redirectAttrs.addFlashAttribute(
                        "notification",
                        "error:We have high volume of applications, approval might take 1-2 days!");
                return "redirect:/login";
            }
            if (encoder.matches(password, user.get().getPassword())) {
                session.setAttribute("username", user.get().getUsername());
                if (next != null && !next.isEmpty()) {
                    return "redirect:" + next;
                }
                return "redirect:/";
            }
        }

        model.addAttribute("error", true);
        return "login";
    }

    @GetMapping("/logout")
    public String logoutHandler(HttpSession session) {
        session.removeAttribute("username");
        return "redirect:/";
    }

    @PostMapping("/password-recovery")
    public String passwordRecoveryGetHandler(@RequestParam String email,
                                             RedirectAttributes redirectAttrs) {
        Optional<User> userOptional = userRepository.findByEmail(email.trim().toLowerCase());

        if (userOptional.isEmpty()) {
            // Don't let them know if the email exist in the system
            redirectAttrs.addFlashAttribute(
                    "notification",
                    "info:After the admin approval, an email will be sent to your email address. Please check your spam folder just in case!");
            return "redirect:/";
        }

        PasswordRecovery passwordRecovery =
                new PasswordRecovery(userOptional.get(), generateToken(16));
        passwordRecoveryRepository.save(passwordRecovery);

        redirectAttrs.addFlashAttribute(
                "notification",
                "info:After the admin approval, an email will be sent to your email address. Please check your spam folder just in case!");
        return "redirect:/";
    }

    @GetMapping("/new-password/{token}")
    public String newPasswordGetHandler(@PathVariable String token, Model model) {
        model.addAttribute("title", "New Password - ShopSmart");

        Optional<PasswordRecovery> passwordRecovery = passwordRecoveryRepository.findByToken(token);

        if (passwordRecovery.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("passwordRecoveryDto", new PasswordRecoveryDto());
        return "new_password";
    }

    @PostMapping("/new-password/{token}")
    public String newPasswordPostHandler(@PathVariable String token,
                                         @Valid
                                         @ModelAttribute PasswordRecoveryDto passwordRecoveryDto,
                                         BindingResult bindingResult, Model model) {
        model.addAttribute("title", "New Password - ShopSmart");

        Optional<PasswordRecovery> passwordRecovery = passwordRecoveryRepository.findByToken(token);

        if (passwordRecovery.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!Objects.equals(passwordRecoveryDto.getNewPassword1(),
                            passwordRecoveryDto.getNewPassword2())) {
            bindingResult.addError(new FieldError("passwordRecoveryDto", "newPassword1",
                                                  "Passwords do not match!"));
        }

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "new_password";
        }

        User user = passwordRecovery.get().getUser();
        user.setPassword(encoder.encode(passwordRecoveryDto.getNewPassword1()));
        userRepository.save(user);
        passwordRecoveryRepository.delete(passwordRecovery.get());
        return "redirect:/login";
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

        // The null and blank cases are handled in the UserDto.accountType @NotBlank annotation.
        if (userDto.getAccountType() != null && !Objects.equals(userDto.getAccountType(), "")
            && !Objects.equals(userDto.getAccountType(), "customer")
            && !Objects.equals(userDto.getAccountType(), "merchant")) {
            bindingResult.addError(new FieldError(
                    "userDto", "accountType", "Account type must be either Customer or Merchant!"));
        }

        if (!Objects.equals(userDto.getPassword1(), userDto.getPassword2())) {
            bindingResult.addError(
                    new FieldError("userDto", "password1", "Passwords do not match!"));
        }
    }

    /**
     * Generates a random token of the specified length.
     * <p>
     * This method uses a {@link SecureRandom} instance to generate a random byte array of the given
     * length. Each byte in the array is then converted to a two-character hexadecimal string
     * representation using zero-padding. Finally, all the hexadecimal strings are concatenated and
     * returned as a single String.
     *
     * @param length the desired length of the token (in bytes)
     * @return a random token string of the specified length, or null if an error occurs during
     *         generation
     * @throws IllegalArgumentException if the provided length is less than or equal to zero
     */
    private String generateToken(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        StringBuilder token = new StringBuilder(length * 2);

        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }

        return token.toString();
    }
}
