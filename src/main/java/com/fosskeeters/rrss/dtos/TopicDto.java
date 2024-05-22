package com.fosskeeters.rrss.dtos;

import com.fosskeeters.rrss.models.Topic;

import jakarta.validation.constraints.*;

public class TopicDto {
    @NotNull
    @Size(min = 1, max = 64, message = "Title can not be longer than 64 characters!")
    private String title;

    @NotBlank(message = "Post type is required!")
    private String topicType;

    public TopicDto() {}

    public TopicDto(Topic topic) {
        this.title = topic.getTitle();
        switch (topic.getType()) {
            case Topic.Type.DISCUSSION:
                this.topicType = "discussion";
                break;

            case Topic.Type.TUTORIAL:
                this.topicType = "tutorial";
                break;

            case Topic.Type.QNA:
                this.topicType = "qna";
                break;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public String getTopicType() {
        return topicType;
    }

    public void setTopicType(String topicType) {
        this.topicType = topicType;
    }
}
