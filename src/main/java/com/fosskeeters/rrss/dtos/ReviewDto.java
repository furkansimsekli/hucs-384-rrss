package com.fosskeeters.rrss.dtos;

import com.fosskeeters.rrss.models.Review;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewDto {
    @NotNull
    @Size(min = 3, max = 32, message = "Review title length must be between 3-32 characters")
    private String title;

    @NotNull
    @Size(min = 10, max = 4096, message = "Review length must be between 10-4096 characters")
    private String body;

    @NotNull
    @Range(min = 1, max = 5, message = "Score must be between 1-5")
    private int score;

    public ReviewDto() {}

    public ReviewDto(Review review) {
        this.title = review.getTitle();
        this.body = review.getBody();
        this.score = review.getScore();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body.trim();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
