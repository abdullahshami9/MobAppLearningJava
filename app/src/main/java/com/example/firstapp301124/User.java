package com.example.firstapp301124;
public class User {
    private String name;
    private String email;
    private String link;

    // Constructor
    public User(String name, String email, String link) {
        this.name = name;
        this.email = email;
        this.link = link;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLink() {
        return link;
    }
}
