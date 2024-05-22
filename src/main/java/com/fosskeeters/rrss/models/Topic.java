package com.fosskeeters.rrss.models;

import com.fosskeeters.rrss.dtos.TopicDto;

import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Topic {
    public enum Type { DISCUSSION, TUTORIAL, QNA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 64)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private User owner;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Entry> entries;

    @CreatedDate
    private LocalDateTime createdAt;

    @Column
    private Type type;

    public Topic() {
        this.createdAt = LocalDateTime.now();
        this.entries = new ArrayList<>();
    }

    public Topic(TopicDto topicDto, User owner) {
        this.title = topicDto.getTitle();
        switch (topicDto.getTopicType()) {
            case "discussion":
                this.type = Type.DISCUSSION;
                break;
            case "tutorial":
                this.type = Type.TUTORIAL;
                break;
            case "qna":
                this.type = Type.QNA;
                break;
        }
        this.entries = new ArrayList<>();
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void updateFromDto(TopicDto topicDto) {
        this.title = topicDto.getTitle();
        switch (topicDto.getTopicType()) {
            case "discussion":
                this.type = Type.DISCUSSION;
                break;
            case "tutorial":
                this.type = Type.TUTORIAL;
                break;
            case "qna":
                this.type = Type.QNA;
                break;
        }
    }
}
