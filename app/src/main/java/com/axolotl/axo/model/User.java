package com.axolotl.axo.model;

import java.util.Map;

public class User {
    public String email;
    public String password;
    public String name;
    public String type;
    public String activeController;
    public Map<String, String> controllers;

    public User() {
    }

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.type = "member";
    }

    public User(String email, String password, String name, String type) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.type = type;
    }
}
