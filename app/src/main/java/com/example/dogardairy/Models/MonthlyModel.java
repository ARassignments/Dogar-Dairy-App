package com.example.dogardairy.Models;

public class MonthlyModel {
    String id, name , contact, balance, MonthlyDetail;

    public MonthlyModel(){

    }

    public MonthlyModel(String id, String name, String contact, String balance, String MonthlyDetail) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.balance = balance;
        this.MonthlyDetail = MonthlyDetail;
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
}
