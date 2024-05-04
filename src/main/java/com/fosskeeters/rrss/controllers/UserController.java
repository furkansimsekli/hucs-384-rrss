package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.ChangePasswordDto;
import com.fosskeeters.rrss.dtos.UserUpdateDto;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {
    private UserRepository userRepository;
    private Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"/user", "/user/"})
    public String getUserRedirectHandler(HttpSession session) {
        var authenticationRedirect = checkAuthentication(session);

        if (authenticationRedirect.isPresent()) {
            return authenticationRedirect.get();
        }

        return "redirect:/user/" + session.getAttribute("username").toString();
    }

    @GetMapping("/user/{usernameParam}")
    public String userProfileHandler(Model model, @PathVariable String usernameParam,
                                     HttpSession session) {
        var authorizationRedirect = checkAuthorization(session, model, usernameParam);
        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }

        User displayedUser = userRepository.findByUsername(usernameParam).get();

        // Logged in user is either an admin or the displayed user.
        UserUpdateDto userUpdateDto = UserUpdateDto.fromUser(displayedUser);
        model.addAttribute("userUpdateDto", userUpdateDto);
        return "user";
    }

    @PostMapping("/user/{usernameParam}")
    public String userUpdateHandler(Model model, @PathVariable String usernameParam,
                                    HttpSession session,
                                    @Valid @ModelAttribute UserUpdateDto userUpdateDto,
                                    BindingResult bindingResult) {
        var authorizationRedirect = checkAuthorization(session, model, usernameParam);
        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }
        User displayedUser = userRepository.findByUsername(usernameParam).get();

        validateUserUpdate(userUpdateDto, bindingResult, displayedUser);

        if (bindingResult.hasErrors()) {
            model.addAttribute("hasErrors", "true");
            System.out.println(bindingResult);
            return "user";
        }

        User user = new User();
        user.setId(displayedUser.getId());
        user.setUsername(displayedUser.getUsername());
        user.setPassword(displayedUser.getPassword());
        user.setType(displayedUser.getType());
        user.setProfileImagePath(userUpdateDto.getProfileImagePath());
        user.setAddress(userUpdateDto.getAddress());
        user.setDateOfBirth(userUpdateDto.getDateOfBirth());
        user.setEmail(userUpdateDto.getEmail());
        user.setFirstName(userUpdateDto.getFirstName());
        user.setLastName(userUpdateDto.getLastName());
        user.setPhoneNumber(userUpdateDto.getPhoneNumber());
        userRepository.save(user);

        model.addAttribute("updatedSuccessfully", "true");
        return "user";
    }

    @GetMapping("/user/{usernameParam}/change-password")
    public String getChangePasswordHandler(Model model, @PathVariable String usernameParam,
                                           HttpSession session) {
        var authorizationRedirect = checkAuthorization(session, model, usernameParam);

        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }

        ChangePasswordDto dto = new ChangePasswordDto();
        model.addAttribute("changePasswordDto", dto);
        return "change_password";
    }

    @PostMapping("/user/{usernameParam}/change-password")
    public String postChangePasswordHandler(Model model, @PathVariable String usernameParam,
                                            HttpSession session,
                                            @Valid @ModelAttribute ChangePasswordDto dto,
                                            BindingResult bindingResult) {
        var authorizationRedirect = checkAuthorization(session, model, usernameParam);

        if (authorizationRedirect.isPresent()) {
            return authorizationRedirect.get();
        }

        User displayedUser = userRepository.findByUsername(usernameParam).get();
        validateChangePassword(dto, bindingResult, displayedUser);

        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult);
            return "change_password";
        }

        displayedUser.setPassword(encoder.encode(dto.getNewPassword1()));
        userRepository.save(displayedUser);
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

        Optional<User> loggedInUser = userRepository.findByUsername(loggedInUsername.toString());
        if (loggedInUser.isEmpty()) {
            // This condition should never be met but can't be too safe :)
            return Optional.of("redirect:/login");
        }

        return Optional.empty(); // keep going
    }

    private Optional<String> checkAuthorization(HttpSession session, Model model,
                                                String usernameParam) {
        var authenticationRedirect = checkAuthentication(session);
        if (authenticationRedirect.isPresent()) {
            return authenticationRedirect;
        }

        Optional<User> displayedUser = userRepository.findByUsername(usernameParam);
        if (displayedUser.isEmpty()) {
            model.addAttribute("errorString", "No such user found");
            return Optional.of("user");
        }

        User loggedInUser =
                userRepository.findByUsername(session.getAttribute("username").toString()).get();
        if (loggedInUser.getType() != User.Type.ADMIN
            && !loggedInUser.getUsername().equals(displayedUser.get().getUsername())) {
            // the logged in user and displayed user are different
            model.addAttribute("errorString", "Unauthorized access");
            return Optional.of("user");
        }

        return Optional.empty();
    }
}
