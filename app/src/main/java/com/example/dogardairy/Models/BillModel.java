package com.example.dogardairy.Models;

public class BillModel {
    String id, userId, userPerson, name, contact, milkRate, paymentMethod, totalAmount, givenAmount, totalQty, balanceAmount, from, to, month, date;

    public BillModel() {
    }

    public BillModel(String id, String userId, String userPerson, String name, String contact, String milkRate, String paymentMethod, String totalAmount, String givenAmount, String totalQty, String balanceAmount, String from, String to, String month, String date) {
        this.id = id;
        this.userId = userId;
        this.userPerson = userPerson;
        this.name = name;
        this.contact = contact;
        this.milkRate = milkRate;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.givenAmount = givenAmount;
        this.totalQty = totalQty;
        this.balanceAmount = balanceAmount;
        this.from = from;
        this.to = to;
        this.month = month;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPerson() {
        return userPerson;
    }

    public void setUserPerson(String userPerson) {
        this.userPerson = userPerson;
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

    public String getMilkRate() {
        return milkRate;
    }

    public void setMilkRate(String milkRate) {
        this.milkRate = milkRate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getGivenAmount() {
        return givenAmount;
    }

    public void setGivenAmount(String givenAmount) {
        this.givenAmount = givenAmount;
    }

    public String getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(String totalQty) {
        this.totalQty = totalQty;
    }

    public String getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(String balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
