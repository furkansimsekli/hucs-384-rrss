package com.fosskeeters.rrss.models;

import com.fosskeeters.rrss.dtos.ReviewDto;
import com.fosskeeters.rrss.dtos.ReviewReplyDto;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private User author;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private Product product;

    @Column(length = 32)
    private String title;

    @Column(length = 4096)
    private String body;

    @Column
    @Range(min = 1, max = 5)
    private int score;

    @Column(length = 4096)
    private String merchantReplyBody;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "review", cascade = CascadeType.ALL)
    private List<Vote> votes;

    public Review() {}

    public Review(ReviewDto reviewDto) {
        this.title = reviewDto.getTitle();
        this.body = reviewDto.getBody();
        this.score = reviewDto.getScore();
        this.createdAt = LocalDateTime.now();
    }

    public void setFromReviewDto(ReviewDto reviewDto) {
        this.title = reviewDto.getTitle();
        this.body = reviewDto.getBody();
        this.score = reviewDto.getScore();
    }

    public void setFromReviewReplyDto(ReviewReplyDto reviewReplyDto) {
        this.merchantReplyBody = reviewReplyDto.getMerchantReplyBody();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getMerchantReplyBody() {
        return merchantReplyBody;
    }

    public void setMerchantReplyBody(String merchantReplyBody) {
        this.merchantReplyBody = merchantReplyBody;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    public int getLikeCount() {
        int likeCount = 0;
        int dislikeCount = 0;
        if (votes != null) {
            for(Vote vote : votes) {
                if (vote.isValue()) {
                    likeCount++;
                } else {
                    dislikeCount++;
                }
            }
        }
        return likeCount;
    }

    public int getDislikeCount() {
        return votes != null ? votes.size() - getLikeCount() : 0;
    }
}
