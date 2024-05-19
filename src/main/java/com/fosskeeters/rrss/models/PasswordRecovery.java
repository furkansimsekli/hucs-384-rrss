package com.fosskeeters.rrss.models;

import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class PasswordRecovery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String token;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private User user;

    @Column
    private boolean isEmailSent;

    @CreatedDate
    private LocalDateTime createdAt;

    public PasswordRecovery() {}

    public PasswordRecovery(User user, String token) {
        this.user = user;
        this.token = token;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isEmailSent() {
        return isEmailSent;
    }

    public void setEmailSent(boolean emailSent) {
        isEmailSent = emailSent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
