package com.example.dogardairy.Models;

public class UsersModel {
    String id, name, email, image, role, milkRate, stockRate, created_on, status;

    public UsersModel(){

    }

    public UsersModel(String id, String name, String email, String image, String role, String milkRate, String stockRate, String created_on, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.image = image;
        this.role = role;
        this.milkRate = milkRate;
        this.stockRate = stockRate;
        this.created_on = created_on;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public String getRole() {
        return role;
    }

    public String getCreated_on() {
        return created_on;
    }

    public String getStatus() {
        return status;
    }

    public String getMilkRate() {
        return milkRate;
    }

    public void setMilkRate(String milkRate) {
        this.milkRate = milkRate;
    }

    public String getStockRate() {
        return stockRate;
    }

    public void setStockRate(String stockRate) {
        this.stockRate = stockRate;
    }
}
