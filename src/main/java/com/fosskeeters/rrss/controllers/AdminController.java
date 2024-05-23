package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.PasswordRecovery;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.PasswordRecoveryRepository;
import com.fosskeeters.rrss.repositories.UserRepository;
import com.fosskeeters.rrss.services.EmailService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final PasswordRecoveryRepository passwordRecoveryRepository;
    private final EmailService emailService;

    public AdminController(UserRepository userRepository,
                           PasswordRecoveryRepository passwordRecoveryRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordRecoveryRepository = passwordRecoveryRepository;
        this.emailService = emailService;
    }

    @GetMapping("/signup-requests")
    public String signupRequests(HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/admin/signup-requests";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/admin/signup-requests";
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
            return "redirect:/login?next=/admin/" + username + "/approve";
        }

        String authenticatedUsername = session.getAttribute("username").toString();
        Optional<User> authenticatedUser = userRepository.findByUsername(authenticatedUsername);
        Optional<User> awaitingUser = userRepository.findByUsername(username);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/admin/" + username + "/approve";
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
            return "redirect:/login?next=/admin/" + username + "/reject";
        }

        String authenticatedUsername = session.getAttribute("username").toString();
        Optional<User> authenticatedUser = userRepository.findByUsername(authenticatedUsername);
        Optional<User> awaitingUser = userRepository.findByUsername(username);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/admin/" + username + "/reject";
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

    @GetMapping("/password-recovery-requests")
    public String passwordRecoveryRequests(HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/admin/password-recovery-requests";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/admin/password-recovery-requests";
        }

        if (user.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<PasswordRecovery> recoveryRequests =
                passwordRecoveryRepository.findAllByIsEmailSentIsFalseOrderByCreatedAtAsc();
        model.addAttribute("recoveryRequests", recoveryRequests);
        return "admin/password_recovery_requests";
    }

    @GetMapping("/{passwordRecoveryId}/send-password-recovery-email")
    public String sendPasswordRecoveryEmail(@PathVariable long passwordRecoveryId,
                                            HttpSession session, RedirectAttributes redirectAttrs) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/admin/" + passwordRecoveryId
                    + "/send-password-recovery-email";
        }

        String authenticatedUsername = session.getAttribute("username").toString();
        Optional<User> authenticatedUser = userRepository.findByUsername(authenticatedUsername);
        Optional<PasswordRecovery> awaitingRequest =
                passwordRecoveryRepository.findByIdAndIsEmailSentIsFalse(passwordRecoveryId);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/admin/" + passwordRecoveryId
                    + "/send-password-recovery-email";
        }

        if (awaitingRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (authenticatedUser.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        User user = awaitingRequest.get().getUser();
        String token = awaitingRequest.get().getToken();
        String body = "Click the link below to reset your password: <br/>"
                + "http://localhost:8080/new-password/" + token;

        // DEBUG
        if (user.getEmail().endsWith("@example.com")) {
            System.out.println(body);
            redirectAttrs.addFlashAttribute(
                    "notification", "info:Email body has been printed out to standard output!");
            awaitingRequest.get().setEmailSent(true);
            passwordRecoveryRepository.save(awaitingRequest.get());
            return "redirect:/admin/password-recovery-requests";
        }

        try {
            emailService.send(/*to=*/user.getEmail(), /*subject=*/"Password Recovery",
                              /*content=*/body);
            awaitingRequest.get().setEmailSent(true);
            passwordRecoveryRepository.save(awaitingRequest.get());
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        redirectAttrs.addFlashAttribute("notification",
                                        "success:Password reset link has been sent to the user!");
        return "redirect:/admin/password-recovery-requests";
    }

    @GetMapping("/{passwordRecoveryId}/reject-password-recovery")
    public String rejectPasswordRecovery(@PathVariable long passwordRecoveryId, HttpSession session,
                                         RedirectAttributes redirectAttrs) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login?next=/admin/" + passwordRecoveryId
                    + "/reject-password-recovery";
        }

        String authenticatedUsername = session.getAttribute("username").toString();
        Optional<User> authenticatedUser = userRepository.findByUsername(authenticatedUsername);
        Optional<PasswordRecovery> awaitingRequests =
                passwordRecoveryRepository.findByIdAndIsEmailSentIsFalse(passwordRecoveryId);

        if (authenticatedUser.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login?next=/admin/" + passwordRecoveryId
                    + "/reject-password-recovery";
        }

        if (awaitingRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (authenticatedUser.get().getType() != User.Type.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        passwordRecoveryRepository.delete(awaitingRequests.get());
        redirectAttrs.addFlashAttribute("notification", "success:Request has been rejected!");
        return "redirect:/admin/password-recovery-requests";
    }
}
