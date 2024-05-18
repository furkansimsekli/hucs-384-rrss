package com.fosskeeters.rrss.models;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private Product product;

    @Column
    @Range(max = 9)
    private int displayOrder;

    @Column(length = 128)
    private String caption;

    @Column
    private String imagePath;

    @CreatedDate
    private LocalDateTime createdAt;

    public ProductImage() {}

    public ProductImage(Product product, int displayOrder, String caption, String imagePath) {
        this.product = product;
        this.displayOrder = displayOrder;
        this.caption = caption;
        this.imagePath = imagePath;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
