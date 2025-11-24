package com.example.smartfireapp;

// 1. Data Model
public class NewsletterSubscriber {

    private String email;

    // Default constructor required for calls to DataSnapshot.getValue(NewsletterSubscriber.class)
    public NewsletterSubscriber() {
    }

    public NewsletterSubscriber(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}