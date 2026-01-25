package com.andrewsmith.financestracker.model;

import org.yaml.snakeyaml.tokens.ScalarToken;

public class Category {
    private Long id;
    private String name;


    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
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
