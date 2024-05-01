package com.fosskeeters.rrss.models;

import com.fosskeeters.rrss.dtos.ProductDto;
import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDateTime;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128)
    private String name;

    @Column(length = 4096)
    private String description;

    @Column
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private double price;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn
    private User owner;

    @CreatedDate
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(User owner, ProductDto productDto) {
        this.owner = owner;
        this.name = productDto.getName();
        this.description = productDto.getDescription();
        this.price = productDto.getPrice();
        this.createdAt = LocalDateTime.now();
    }

    public void setFromProductDto(ProductDto productDto) {
        this.name = productDto.getName();
        this.description = productDto.getDescription();
        this.price = productDto.getPrice();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}
