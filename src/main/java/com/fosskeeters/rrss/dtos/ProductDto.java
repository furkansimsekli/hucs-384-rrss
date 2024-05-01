package com.fosskeeters.rrss.dtos;

import jakarta.validation.constraints.*;

public class ProductDto {

    @NotNull
    @Size(min = 1, max = 128, message = "Name can not be longer than 128 characters!")
    private String name;

    @NotNull
    @Size(max = 4096, message = "Description can not be longer than 4096 characters!")
    private String description;

    @NotNull(message = "Price can not be empty!")
    private double price;

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
}
