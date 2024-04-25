package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.UserDto;
import com.fosskeeters.rrss.dtos.UserUpdateDto;
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

    @GetMapping({"/user", "/user/"})
    public String getUserRedirectHandler(HttpSession session) {
        Object loggedInUsername = session.getAttribute("username");
        if (loggedInUsername == null) {
            return "redirect:/login";
        }

        Optional<User> loggedInUser = userRepository.findByUsername(loggedInUsername.toString());
        if (loggedInUser.isEmpty()) {
            // This condition should never be met but can't be too safe :)
            return "redirect:/login";
        }

        return "redirect:/user/" + loggedInUsername;
    }

    @GetMapping("/user/{usernameParam}")
    public String userProfileHandler(Model model, @PathVariable String usernameParam, HttpSession session) {
        Object loggedInUsername = session.getAttribute("username");
        if (loggedInUsername == null) {
            return "redirect:/login";
        }

        Optional<User> loggedInUser = userRepository.findByUsername(loggedInUsername.toString());
        if (loggedInUser.isEmpty()) {
            // This condition should never be met but can't be too safe :)
            return "redirect:/login";
        }

        Optional<User> displayedUser = userRepository.findByUsername(usernameParam);
        if (displayedUser.isEmpty()) {
            model.addAttribute("errorString", "No such user found");
            return "user";
        }

        if (loggedInUser.get().getType() != 1 // if user is not an Admin (TODO avoid magic numbers)
                && !loggedInUser.get().getUsername().equals(displayedUser.get().getUsername())) {
            // the logged in user and displayed user are different
            model.addAttribute("errorString", "Unauthorized access");
            return "user";
        }

        // Logged in user is either an admin or the displayed user.
        UserUpdateDto userUpdateDto = UserUpdateDto.fromUser(displayedUser.get());
        model.addAttribute("userUpdateDto", userUpdateDto);
        return "user";
    }

    @PostMapping("/user/{usernameParam}")
    public String userUpdateHandler(Model model, @PathVariable String usernameParam, HttpSession session,
            @Valid @ModelAttribute UserUpdateDto userUpdateDto, BindingResult bindingResult) {
        Object loggedInUsername = session.getAttribute("username");
        if (loggedInUsername == null) {
            return "redirect:/login";
        }

        Optional<User> loggedInUser = userRepository.findByUsername(loggedInUsername.toString());
        if (loggedInUser.isEmpty()) {
            // This condition should never be met but can't be too safe :)
            return "redirect:/login";
        }

        Optional<User> displayedUser = userRepository.findByUsername(usernameParam);
        if (displayedUser.isEmpty()) {
            model.addAttribute("errorString", "No such user found");
            return "user";
        }

        if (loggedInUser.get().getType() != 1 // if user is not an Admin (TODO avoid magic numbers)
                && !loggedInUser.get().getUsername().equals(displayedUser.get().getUsername())) {
            // the logged in user and displayed user are different
            model.addAttribute("errorString", "Unauthorized access");
            return "user";
        }

        validateUserUpdate(userUpdateDto, bindingResult, displayedUser.get());

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", "true");
            System.out.println(bindingResult);
            return "user";
        }

        User user = new User();
        user.setId(displayedUser.get().getId());
        user.setUsername(displayedUser.get().getUsername());
        user.setPassword(displayedUser.get().getPassword());
        user.setType(displayedUser.get().getType());
        user.setProfileImagePath(userUpdateDto.getProfileImagePath());
        user.setAddress(userUpdateDto.getAddress());
        user.setDateOfBirth(userUpdateDto.getDateOfBirth());
        user.setEmail(userUpdateDto.getEmail());
        user.setFirstName(userUpdateDto.getFirstName());
        user.setLastName(userUpdateDto.getLastName());
        user.setPhoneNumber(userUpdateDto.getPhoneNumber());
        userRepository.save(user);

        //model.addAttribute("userUpdateDbo", user);
        model.addAttribute("updatedSuccessfully", "true");
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

    private void validateUserUpdate(UserUpdateDto userUpdateDto, BindingResult bindingResult, User oldUser) {
        if (!oldUser.getEmail().equals(userUpdateDto.getEmail())
                && userRepository.existsByEmail(userUpdateDto.getEmail())) {
            bindingResult.addError(
                    new FieldError("userUpdateDto", "email", "This email address is already taken!"));
        }

        if (!oldUser.getPhoneNumber().equals(userUpdateDto.getPhoneNumber())
                && userRepository.existsByPhoneNumber(userUpdateDto.getPhoneNumber())) {
            bindingResult.addError(
                    new FieldError(
                            "userUpdateDto", "phoneNumber", "This phone number is already taken!"));
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
