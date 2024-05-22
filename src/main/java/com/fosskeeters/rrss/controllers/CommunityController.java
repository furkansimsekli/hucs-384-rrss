package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.EntryDto;
import com.fosskeeters.rrss.dtos.TopicDto;
import com.fosskeeters.rrss.models.Topic;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.EntryRepository;
import com.fosskeeters.rrss.repositories.TopicRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/community")
public class CommunityController {
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final EntryRepository entryRepository;

    public CommunityController(UserRepository userRepository, TopicRepository topicRepository,
            EntryRepository entryRepository) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.entryRepository = entryRepository;
    }

    @GetMapping("")
    public String indexRedirect() {
        return "redirect:/community/topics";
    }

    @GetMapping("/topics")
    public String getTopicsHandler(HttpSession session, Model model) {
        var allTopics = topicRepository.findAll();
        Collections.sort(allTopics, Comparator.comparing(Topic::getCreatedAt));

        var discussions = allTopics.stream()
                                  .filter(topic -> topic.getType() == Topic.Type.DISCUSSION)
                                  .sorted(Comparator.comparing(Topic::getCreatedAt))
                                  .toList();
        var tutorials = allTopics.stream()
                                .filter(topic -> topic.getType() == Topic.Type.TUTORIAL)
                                .sorted(Comparator.comparing(Topic::getCreatedAt))
                                .toList();
        var qna = allTopics.stream()
                          .filter(topic -> topic.getType() == Topic.Type.QNA)
                          .sorted(Comparator.comparing(Topic::getCreatedAt))
                          .toList();

        var topicsMap = new LinkedHashMap<String, List<Topic>>();
        topicsMap.put("All Topics", allTopics);
        topicsMap.put("Discussions", discussions);
        topicsMap.put("Tutorials", tutorials);
        topicsMap.put("Q&A", qna);
        model.addAttribute("topicsMap", topicsMap);

        var currentUser = userRepository.findByUsername((String) session.getAttribute("username"));
        if (currentUser.isPresent()) {
            var user = currentUser.get();
            if (user.getType() == User.Type.ADMIN || user.getType() == User.Type.COMMUNITY_MOD) {
                model.addAttribute("isModOrAdmin", true);
            }
        }

        return "community/topics";
    }

    @GetMapping("/topics/{topicId}")
    public String getEntriesHandler(HttpSession session, Model model, @PathVariable long topicId) {
        Optional<Topic> topic = topicRepository.findById(topicId);

        if (topic.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("topic", topic.get());
        model.addAttribute("entryDto", new EntryDto());

        return "community/entries";
    }

    @GetMapping("/topics/create")
    public String getTopicCreate(HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        model.addAttribute("topicDto", new TopicDto());
        model.addAttribute("newTopic", true);

        return "community/update_topic";
    }

    @PostMapping("/topics/create")
    public String postTopicCreate(HttpSession session, @Valid @ModelAttribute TopicDto topicDto,
            BindingResult bindingResult, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("topicDto", topicDto);
            model.addAttribute("hasErrors", true);
            model.addAttribute("newTopic", true);

            return "community/update_topic";
        }

        Topic topic = new Topic(topicDto, userOpt.get());
        topicRepository.save(topic);
        model.addAttribute("notificationMessage", "Voila! Your post has been submitted.");
        return "redirect:/community/topics";
    }

    @GetMapping("/topics/{topicId}/delete")
    public String deleteTopic(HttpSession session, @PathVariable long topicId, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        Optional<Topic> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Only admins, mods and the topic owner can delete the topic
        if (user.get().getId() != topic.get().getOwner().getId()
                && user.get().getType() != User.Type.ADMIN
                && user.get().getType() != User.Type.COMMUNITY_MOD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        topicRepository.delete(topic.get());
        model.addAttribute("notificationMessage", "Oh no! Where did your post go?");
        return "redirect:/community/topics";
    }

    @GetMapping("/topics/{topicId}/update")
    public String getUpdateTopic(HttpSession session, @PathVariable long topicId, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        Optional<Topic> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Only admins, mods and the topic owner can update the topic
        if (user.get().getId() != topic.get().getOwner().getId()
                && user.get().getType() != User.Type.ADMIN
                && user.get().getType() != User.Type.COMMUNITY_MOD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        TopicDto topicDto = new TopicDto(topic.get());
        model.addAttribute("topicDto", topicDto);
        model.addAttribute("newTopic", false);

        return "community/update_topic";
    }

    @PostMapping("/topics/{topicId}/update")
    public String postUpdateTopic(HttpSession session, @Valid @ModelAttribute TopicDto topicDto,
            BindingResult bindingResult, Model model, @PathVariable long topicId) {
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String username = session.getAttribute("username").toString();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            session.removeAttribute("username");
            return "redirect:/login";
        }

        Optional<Topic> topicOpt = topicRepository.findById(topicId);
        if (topicOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Topic topic = topicOpt.get();

        // Only admins, mods and the topic owner can delete the topic
        if (user.get().getId() != topic.getOwner().getId()
                && user.get().getType() != User.Type.ADMIN
                && user.get().getType() != User.Type.COMMUNITY_MOD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("topicDto", topicDto);
            model.addAttribute("hasErrors", true);
            model.addAttribute("newTopic", false);

            return "community/update_topic";
        }

        topic.updateFromDto(topicDto);
        topicRepository.save(topic);
        model.addAttribute("notificationMessage", "Voila! Your post has been updated.");
        return "redirect:/community/topics";
    }
}
