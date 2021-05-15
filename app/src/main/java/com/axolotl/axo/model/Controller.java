package com.axolotl.axo.model;

import java.util.ArrayList;
import java.util.Map;

public class Controller {
    public String id;
    public String password;
    public String name;
    public Map<String, Pin> pins;
    public String summary;

    public Controller() {
    }

    public Controller(String id, String password, Map<String, Pin> pins) {
        this.id = id;
        this.password = password;
        this.name = id+"_name";
        this.pins = pins;
        StringBuilder _summary = new StringBuilder();
        for (String pin: pins.keySet()){
            _summary.append("P").append(pin.replace("pin_", "")).append("S0");
        }
        this.summary = _summary.toString();
    }

    public Controller(String id, String password, String name, Map<String, Pin> pins) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.pins = pins;
    }

    public Controller(String id, String password, String name, Map<String, Pin> pins, String summary) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.pins = pins;
        this.summary = summary;
    }
}
