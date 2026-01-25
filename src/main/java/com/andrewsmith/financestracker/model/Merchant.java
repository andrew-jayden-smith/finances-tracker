package com.andrewsmith.financestracker.model;

public class Merchant {
    private Long id;
    private String name;

    // Mapper
    public Merchant() {}

    // creating a new merchant
    public Merchant(String name) {
        this.name = name;
    }

    // Load existing from db
    public Merchant(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
