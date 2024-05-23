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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final Argon2PasswordEncoder encoder;

    public UserController(UserRepository userRepository) throws IOException {
        this.userRepository = userRepository;
        this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

        // FIXME: This should be variable instead of hardcoded.
        Files.createDirectories(Paths.get("public", "profile-images"));
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
        model.addAttribute("user", displayedUser.get());
        return "user/profile";
    }

    @PostMapping("/{usernameParam}")
    public String userUpdateHandler(Model model, @PathVariable String usernameParam,
                                    HttpSession session,
                                    @Valid @ModelAttribute UserUpdateDto userUpdateDto,
                                    BindingResult bindingResult) throws IOException {
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
            model.addAttribute("user", displayedUser.get());
            System.out.println(bindingResult);
            return "user/profile";
        }

        if (!userUpdateDto.getProfileImageFile().isEmpty()) {
            MultipartFile image = userUpdateDto.getProfileImageFile();
            String storageFileName = LocalDateTime.now() + "_" + image.getOriginalFilename();
            String storagePathStr = "/profile-images/" + storageFileName;

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get("public" + storagePathStr),
                           StandardCopyOption.REPLACE_EXISTING);
            }

            displayedUser.get().setProfileImagePath(storagePathStr);
        }

        displayedUser.get().setFromUserUpdateDto(userUpdateDto);
        userRepository.save(displayedUser.get());
        model.addAttribute("updatedSuccessfully", "true");
        return "redirect:/user/" + displayedUser.get().getUsername();
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
        return "user/change_password";
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
            return "user/change_password";
        }

        displayedUser.get().setPassword(encoder.encode(dto.getNewPassword1()));
        userRepository.save(displayedUser.get());
        model.addAttribute("updatedSuccessfully", "true");
        return "user/change_password";
    }

    @GetMapping("/{username}/delete")
    public String deleteUserHandler(@PathVariable String username, HttpSession session,
                                    RedirectAttributes redirectAttrs) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/user/" + username + "/delete";
        }

        Optional<User> authenticatedUser =
                userRepository.findByUsername((String) session.getAttribute("username"));

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/user/" + username + "/delete";
        }

        Optional<User> targetUser = userRepository.findByUsername(username);

        if (targetUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (targetUser.get().getId() != authenticatedUser.get().getId()
            && authenticatedUser.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        userRepository.delete(targetUser.get());
        session.removeAttribute("username");
        redirectAttrs.addFlashAttribute("notification", "success:Sorry to see you leaving...");
        return "redirect:/";
    }

    /**
     * Validates the data provided in a UserUpdateDto object for updating a user's information.
     * This method checks for the following conflicts and adds error messages to the provided
     * BindingResult object if any are found:
     * <p>
     * 1. Provided email address must be different from tha user's current email, and it must not
     * exist in the system.
     * <p>
     * 2. Provided phone number must be different from tha user's current phone number, and it must
     * not exist in the system.
     *
     * @param userUpdateDto The UserUpdateDto object containing the updated user information.
     * @param bindingResult The BindingResult object to which validation errors will be added.
     * @param oldUser The User object representing the user whose information is being updated.
     */
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

    /**
     * Validates the data provided in a ChangePasswordDto object for changing a user's password. The
     * validations rules are the following:
     * <p>
     * 1. Current password field must match with the old password.
     * <p>
     * 2. Password and re-type password fields must match each other.
     * <p>
     * This method adds error messages to the provided BindingResult object if any validation fails.
     *
     * @param dto The ChangePasswordDto object containing old and new password information.
     * @param bindingResult The BindingResult object to which validation errors will be added.
     * @param displayedUser The User object representing the user whose password is being changed.
     */
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

    /**
     * This method checks if a user is authenticated based on the presence of a username attribute
     * in the provided HttpSession.
     *
     * @param session The HttpSession object containing user session information.
     * @return An Optional<String> containing a redirect URL to the login page if the user is not
     *         authenticated, or Optional.empty() if the user is authenticated (allowing further
     *         processing).
     */
    private Optional<String> checkAuthentication(HttpSession session) {
        Object loggedInUsername = session.getAttribute("username");
        if (loggedInUsername == null) {
            return Optional.of("redirect:/login?next=/user");
        }

        return Optional.empty();
    }

    /**
     * This method checks if a user is authorized to perform an action on a specific user based on
     * their authentication and role.
     *
     * @param session The HttpSession object containing user session information.
     * @param displayedUser The User object representing the user whose information is being
     *         accessed.
     * @return An Optional<String> containing a redirect URL to the login page if the user is not
     *         authenticated, or throws an exception if the user is not authorized. If the user is
     *         authorized, it returns Optional.empty() allowing further processing.
     * @throws ResponseStatusException with HttpStatus.NOT_FOUND if the user is not authenticated
     *         (based on the result of checkAuthentication) or if the user is not authorized (not an
     *         admin or the owner of the displayed user).
     */
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
