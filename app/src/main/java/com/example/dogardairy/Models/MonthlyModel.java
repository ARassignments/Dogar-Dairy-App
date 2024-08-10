package com.example.dogardairy.Models;

public class MonthlyModel {
    String id, name , contact, balance, userId, MonthlyDetail, milkRate;

    public MonthlyModel(){

    }

    public MonthlyModel(String id, String name, String contact, String balance, String userId, String monthlyDetail, String milkRate) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.balance = balance;
        this.userId = userId;
        MonthlyDetail = monthlyDetail;
        this.milkRate = milkRate;
    }

    public String getMonthlyDetail() {
        return MonthlyDetail;
    }

    public void setMonthlyDetail(String monthlyDetail) {
        MonthlyDetail = monthlyDetail;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMilkRate() {
        return milkRate;
    }

    public void setMilkRate(String milkRate) {
        this.milkRate = milkRate;
    }
}
