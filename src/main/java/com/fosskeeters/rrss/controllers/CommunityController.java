package com.fosskeeters.rrss.controllers;

import com.fosskeeters.rrss.dtos.EntryDto;
import com.fosskeeters.rrss.dtos.TopicDto;
import com.fosskeeters.rrss.models.Entry;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
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
        Collections.sort(allTopics, Comparator.comparing(Topic::getCreatedAt).reversed());

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

        var currentUser = userRepository.findByUsername((String) session.getAttribute("username"));
        if (currentUser.isPresent()) {
            var user = currentUser.get();
            if (user.getType() == User.Type.ADMIN || user.getType() == User.Type.COMMUNITY_MOD) {
                model.addAttribute("isModOrAdmin", true);
            }
        }

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
                                  BindingResult bindingResult, RedirectAttributes redirectAttrs,
                                  Model model) {
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
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your post has been submitted.");
        return "redirect:/community/topics";
    }

    @GetMapping("/topics/{topicId}/delete")
    public String deleteTopic(HttpSession session, @PathVariable long topicId,
                              RedirectAttributes redirectAttrs) {
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
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your post has been deleted.");
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
                                  BindingResult bindingResult, RedirectAttributes redirectAttrs,
                                  Model model, @PathVariable long topicId) {
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
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your post has been updated.");
        return "redirect:/community/topics";
    }

    @GetMapping("/topics/{topicId}/entries/{entryId}/update")
    public String getEntryUpdate(HttpSession session, @PathVariable long topicId,
                                 @PathVariable long entryId, Model model) {
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

        Optional<Entry> entry = entryRepository.findById(entryId);
        if (entry.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // If the entry doesn't belong to the given topic
        if (topic.get().getEntries().stream().allMatch(e -> e.getId() != entryId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Only admins, mods and the entry owner can update the entry
        if (user.get().getId() != entry.get().getAuthor().getId()
            && user.get().getType() != User.Type.ADMIN
            && user.get().getType() != User.Type.COMMUNITY_MOD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        EntryDto entryDto = new EntryDto(entry.get());
        model.addAttribute("entryDto", entryDto);
        model.addAttribute("topic", topic.get());

        return "community/update_entry";
    }

    @PostMapping("/topics/{topicId}/entries/{entryId}/update")
    public String postEntryUpdate(HttpSession session, @Valid @ModelAttribute EntryDto entryDto,
                                  BindingResult bindingResult, RedirectAttributes redirectAttrs,
                                  Model model, @PathVariable long topicId,
                                  @PathVariable long entryId) {
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

        Optional<Entry> entryOpt = entryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Entry entry = entryOpt.get();

        // If the entry doesn't belong to the given topic
        if (topic.getEntries().stream().allMatch(e -> e.getId() != entryId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Only admins, mods and the entry owner can update the entry
        if (user.get().getId() != entry.getAuthor().getId()
            && user.get().getType() != User.Type.ADMIN
            && user.get().getType() != User.Type.COMMUNITY_MOD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("entryDto", entryDto);
            model.addAttribute("hasErrors", true);
            model.addAttribute("topic", topic);

            return "community/update_entry";
        }

        entry.setBody(entryDto.getBody());
        topicRepository.save(topic);
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your entry has been updated.");
        return "redirect:/community/topics/" + topic.getId();
    }

    @GetMapping("/topics/{topicId}/entries/{entryId}/delete")
    public String deleteEntry(HttpSession session, RedirectAttributes redirectAttrs,
                              @PathVariable long topicId, @PathVariable long entryId) {
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

        Optional<Entry> entryOpt = entryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Entry entry = entryOpt.get();

        // If the entry doesn't belong to the given topic
        if (topic.getEntries().stream().allMatch(e -> e.getId() != entryId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Only admins, mods and the entry owner can delete the entry
        if (user.get().getId() != entry.getAuthor().getId()
            && user.get().getType() != User.Type.ADMIN
            && user.get().getType() != User.Type.COMMUNITY_MOD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var entries = topic.getEntries();
        entries.remove(entry);
        topic.setEntries(entries);
        topicRepository.save(topic);
        entryRepository.delete(entry);
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your entry has been deleted.");
        return "redirect:/community/topics/" + topic.getId();
    }

    @PostMapping("/topics/{topicId}/entries/create")
    public String createEntry(HttpSession session, @Valid @ModelAttribute EntryDto entryDto,
                              BindingResult bindingResult, RedirectAttributes redirectAttrs,
                              Model model, @PathVariable long topicId) {
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("entryDto", entryDto);
            model.addAttribute("hasErrors", true);
            model.addAttribute("topic", topic);

            return "community/entries";
        }
        Entry entry = new Entry();
        entry.setAuthor(user.get());
        entry.setBody(entryDto.getBody());
        entry.setCreatedAt(LocalDateTime.now());
        entry.setTopic(topic);
        var entries = topic.getEntries();
        entries.add(entry);
        topic.setEntries(entries);

        entryRepository.save(entry);
        redirectAttrs.addFlashAttribute("notification",
                                        "success:Voila! Your entry has been submitted.");
        return "redirect:/community/topics/" + topic.getId();
    }
}
