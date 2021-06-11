package com.axolotl.axo.model;

public class Pin {
    public String id;
    public String name;
    public String state;

    public Pin() {
    }

    public Pin(String id) {
        this.id = id;
        this.name = id + "_name";
        this.state = "off";
    }

    public Pin(String id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }
}
