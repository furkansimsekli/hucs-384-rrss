package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.models.Product;
import com.fosskeeters.rrss.models.Topic;
import com.fosskeeters.rrss.models.User;
import com.fosskeeters.rrss.repositories.EntryRepository;
import com.fosskeeters.rrss.repositories.TopicRepository;
import com.fosskeeters.rrss.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;


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

        return "community/entries";
    }
    
}
