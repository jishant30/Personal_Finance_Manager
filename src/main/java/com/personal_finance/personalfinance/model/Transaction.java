/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.model;

/**
 *
 * @author Jisha
 */


import java.time.LocalDate;

public class Transaction {

    private final int id;
    private final int userId;
    private final int categoryId;
    private final String categoryName;   // denormalised for display
    private final String type;           // "INCOME" or "EXPENSE"
    private final double amount;
    private final String note;
    private final byte[] receipt;        // optional receipt image BLOB
    private final LocalDate date;

    private Transaction(Builder b) {
        this.id = b.id;
        this.userId = b.userId;
        this.categoryId = b.categoryId;
        this.categoryName = b.categoryName;
        this.type = b.type;
        this.amount = b.amount;
        this.note = b.note;
        this.receipt = b.receipt;
        this.date = b.date;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public byte[] getReceipt() {
        return receipt;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format(
                "Transaction{id=%d, type=%s, amount=%.2f, category=%s, date=%s}",
                id, type, amount, categoryName, date);
    }

    // ------------------------------------------------------------------
    public static class Builder {

        // required
        private final int userId;
        private final int categoryId;
        private final double amount;
        private final LocalDate date;
        // optional
        private int id = 0;
        private String categoryName = "";
        private String type = "EXPENSE";
        private String note = "";
        private byte[] receipt = null;

        public Builder(int userId, int categoryId, double amount, LocalDate date) {
            if (amount < 0) {
                throw new IllegalArgumentException("amount cannot be negative");
            }
            if (date == null) {
                throw new IllegalArgumentException("date required");
            }
            this.userId = userId;
            this.categoryId = categoryId;
            this.amount = amount;
            this.date = date;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder categoryName(String name) {
            this.categoryName = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder receipt(byte[] receipt) {
            this.receipt = receipt;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}







