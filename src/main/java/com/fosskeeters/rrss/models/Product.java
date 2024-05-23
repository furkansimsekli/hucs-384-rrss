package com.fosskeeters.rrss.models;

import com.fosskeeters.rrss.dtos.ProductDto;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 128)
    private String name;

    @Column(length = 4096)
    private String description;

    @Column
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    @Min(0)
    private double price;

    @Column
    @Min(0)
    private Integer viewsLastWeek;

    @Column
    @Min(0)
    private Integer viewsLastMonth;

    @Column
    @Min(0)
    private Integer allViews;

    @Column
    @Min(0)
    private Integer wishCount;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private User owner;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<ProductKeyword> keywords = new ArrayList<>();

    public Product() {}

    public Product(User owner, ProductDto productDto) {
        this.owner = owner;
        this.name = productDto.getName();
        this.description = productDto.getDescription();
        this.price = productDto.getPrice();
        this.createdAt = LocalDateTime.now();

        for (String keyword : productDto.getKeywords()) {
            keywords.add(new ProductKeyword(keyword, this));
        }
    }

    public void setFromProductDto(ProductDto productDto) {
        this.name = productDto.getName();
        this.description = productDto.getDescription();
        this.price = productDto.getPrice();

        setKeywords(new ArrayList<>());
        for (String keyword : productDto.getKeywords()) {
            keywords.add(new ProductKeyword(keyword, this));
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public List<ProductKeyword> getKeywords() {
        return this.keywords;
    }

    public void setKeywords(List<ProductKeyword> keywords) {
        this.keywords.clear();
        this.keywords.addAll(keywords);
    }

    public void setViewsLastWeek(int views) {
        this.viewsLastWeek = views;
    }

    public void setViewsLastMonth(int views) {
        this.viewsLastMonth = views;
    }

    public void setAllViews(int views) {
        this.allViews = views;
    }

    public void setWishCount(int count) {
        this.wishCount = count;
    }

    public int getViewsLastWeek() {
        return viewsLastWeek;
    }

    public int getViewsLastMonth() {
        return viewsLastMonth;
    }

    public int getAllViews() {
        return allViews;
    }

    public int getWishCount() {
        return wishCount;
    }

    public double getAverageScore() {
        if (reviews.isEmpty()) {
            return 0;
        }

        double total = 0;
        for (Review review : reviews) {
            total += review.getScore();
        }
        double averageScore = total / reviews.size();
        return Math.round(averageScore * 10) / 10.0;
    }
}
