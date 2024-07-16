package com.example.dogardairy.Models;

public class MonthlyDetailModel {
    String id, amount , qty, date, month, itemName, itemAmount, totalAmount;

    public MonthlyDetailModel() {
    }

    public MonthlyDetailModel(String id, String amount, String qty, String date, String month, String itemName, String itemAmount, String totalAmount) {
        this.id = id;
        this.amount = amount;
        this.qty = qty;
        this.date = date;
        this.month = month;
        this.itemName = itemName;
        this.itemAmount = itemAmount;
        this.totalAmount = totalAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(String itemAmount) {
        this.itemAmount = itemAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
