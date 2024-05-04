package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.ChangePasswordDto;
import com.fosskeeters.rrss.dtos.UserUpdateDto;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
    private UserRepository userRepository;
    private Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"", "/"})
    public String getUserRedirectHandler(HttpSession session) {
        var authenticationRedirect = checkAuthentication(session);

        if (authenticationRedirect.isPresent()) {
            return authenticationRedirect.get();
        }

        return "redirect:/user/" + session.getAttribute("username").toString();
    }

    @GetMapping("/{usernameParam}")
    public String userProfileHandler(Model model, @PathVariable String usernameParam,
                                     HttpSession session) {
        Optional<User> displayedUser = userRepository.findByUsername(usernameParam);

        if (displayedUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var authorizationRedirect = checkAuthorization(session, displayedUser.get());

        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }

        // Logged-in user is either an admin or the displayed user.
        UserUpdateDto userUpdateDto = new UserUpdateDto(displayedUser.get());
        model.addAttribute("userUpdateDto", userUpdateDto);
        return "user";
    }

    @PostMapping("/{usernameParam}")
    public String userUpdateHandler(Model model, @PathVariable String usernameParam,
                                    HttpSession session,
                                    @Valid @ModelAttribute UserUpdateDto userUpdateDto,
                                    BindingResult bindingResult) {
        Optional<User> displayedUser = userRepository.findByUsername(usernameParam);

        if (displayedUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var authorizationRedirect = checkAuthorization(session, displayedUser.get());

        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }

        validateUserUpdate(userUpdateDto, bindingResult, displayedUser.get());

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", "true");
            System.out.println(bindingResult);
            return "user";
        }

        displayedUser.get().setFromUserUpdateDto(userUpdateDto);
        userRepository.save(displayedUser.get());
        model.addAttribute("updatedSuccessfully", "true");
        return "user";
    }

    @GetMapping("/{usernameParam}/change-password")
    public String getChangePasswordHandler(Model model, @PathVariable String usernameParam,
                                           HttpSession session) {
        Optional<User> displayedUser = userRepository.findByUsername(usernameParam);

        if (displayedUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var authorizationRedirect = checkAuthorization(session, displayedUser.get());

        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }

        ChangePasswordDto dto = new ChangePasswordDto();
        model.addAttribute("changePasswordDto", dto);
        return "change_password";
    }

    @PostMapping("/{usernameParam}/change-password")
    public String postChangePasswordHandler(Model model, @PathVariable String usernameParam,
                                            HttpSession session,
                                            @Valid @ModelAttribute ChangePasswordDto dto,
                                            BindingResult bindingResult) {
        Optional<User> displayedUser = userRepository.findByUsername(usernameParam);

        if (displayedUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var authorizationRedirect = checkAuthorization(session, displayedUser.get());

        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }

        validateChangePassword(dto, bindingResult, displayedUser.get());

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "change_password";
        }

        displayedUser.get().setPassword(encoder.encode(dto.getNewPassword1()));
        userRepository.save(displayedUser.get());
        model.addAttribute("updatedSuccessfully", "true");
        return "change_password";
    }

    private void validateUserUpdate(UserUpdateDto userUpdateDto, BindingResult bindingResult,
                                    User oldUser) {
        if (!oldUser.getEmail().equals(userUpdateDto.getEmail())
            && userRepository.existsByEmail(userUpdateDto.getEmail())) {
            bindingResult.addError(new FieldError("userUpdateDto", "email",
                                                  "This email address is already taken!"));
        }

        if (!oldUser.getPhoneNumber().equals(userUpdateDto.getPhoneNumber())
            && userRepository.existsByPhoneNumber(userUpdateDto.getPhoneNumber())) {
            bindingResult.addError(new FieldError("userUpdateDto", "phoneNumber",
                                                  "This phone number is already taken!"));
        }
    }

    private void validateChangePassword(ChangePasswordDto dto, BindingResult bindingResult,
                                        User displayedUser) {
        if (!encoder.matches(dto.getOldPassword(), displayedUser.getPassword())) {
            bindingResult.addError(new FieldError("changePasswordDto", "oldPassword",
                                                  "Current password do not match"));
        }

        if (!Objects.equals(dto.getNewPassword1(), dto.getNewPassword2())) {
            bindingResult.addError(
                    new FieldError("changePasswordDto", "newPassword1", "Passwords do not match!"));
        }
    }

    private Optional<String> checkAuthentication(HttpSession session) {
        Object loggedInUsername = session.getAttribute("username");
        if (loggedInUsername == null) {
            return Optional.of("redirect:/login");
        }

        return Optional.empty(); // keep going
    }

    private Optional<String> checkAuthorization(HttpSession session, User displayedUser) {
        var authenticationRedirect = checkAuthentication(session);
        if (authenticationRedirect.isPresent()) {
            return authenticationRedirect;
        }

        Optional<User> loggedInUser =
                userRepository.findByUsername(session.getAttribute("username").toString());

        if (loggedInUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Only let the admins and the owner proceed
        if (loggedInUser.get().getType() != User.Type.ADMIN
            && !loggedInUser.get().getUsername().equals(displayedUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return Optional.empty();
    }
}
