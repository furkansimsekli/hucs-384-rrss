package com.fosskeeters.rrss.dtos;

import com.fosskeeters.rrss.models.Entry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EntryDto {
    @NotNull(message = "Topic id cannot be null!")
    private long topicId;

    @NotNull
    @Size(min = 10, max = 4096, message = "Entry length must be between 10-4096 characters")
    private String body;

    public EntryDto() {}

    public EntryDto(long topicId) {
        this.topicId = topicId;
    }

    public EntryDto(Entry entry) {
        this.body = entry.getBody();
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body.trim();
    }
}
