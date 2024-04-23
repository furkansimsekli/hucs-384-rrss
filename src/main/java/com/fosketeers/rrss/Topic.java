package com.fosketeers.rrss;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topicID;

    @Column private String title;

    @Column(name = "creator_id")
    private Long creatorID;

    @CreatedDate private LocalDateTime createdAt;

    public Long getTopicID() {
        return topicID;
    }

    public void setTopicID(Long topicID) {
        this.topicID = topicID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCreatorId() {
        return creatorID;
    }

    public void setCreatorId(Long creatorID) {
        this.creatorID = creatorID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
