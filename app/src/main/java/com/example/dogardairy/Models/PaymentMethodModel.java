package com.example.dogardairy.Models;

public class PaymentMethodModel {
    String id, name;

    public PaymentMethodModel() {
    }

    public PaymentMethodModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
