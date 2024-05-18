package com.fosskeeters.rrss.models;

import jakarta.persistence.*;

@Entity
public class ProductKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 256)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private Product product;

    public ProductKeyword() {}

    public ProductKeyword(String keyword, Product product) {
        this.keyword = keyword;
        this.product = product;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
