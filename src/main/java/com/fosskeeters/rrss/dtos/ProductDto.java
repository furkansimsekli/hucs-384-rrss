package com.fosskeeters.rrss.dtos;

import com.fosskeeters.rrss.models.Product;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import jakarta.validation.constraints.*;

public class ProductDto {
    @NotNull
    @Size(min = 1, max = 128, message = "Name can not be longer than 128 characters!")
    private String name;

    @NotNull
    @Size(max = 4096, message = "Description can not be longer than 4096 characters!")
    private String description;

    @NotNull(message = "Price can not be empty!")
    @Min(value = 0, message = "Product can be free, but can not have a negative price!")
    private double price;

    @Size(max = 9, message = "You can choose at most 9 images for a product!")
    private List<MultipartFile> images;

    public ProductDto() {}

    public ProductDto(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
}
