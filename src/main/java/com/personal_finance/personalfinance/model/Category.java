/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.model;

/**
 *
 * @author Jisha
 */
public class Category {
    private final int    id;
    private final String name;
    private final String type;   // "INCOME" or "EXPENSE"

    public Category(int id, String name, String type) {
        this.id   = id;
        this.name = name;
        this.type = type;
    }

    public int    getId()   { return id; }
    public String getName() { return name; }
    public String getType() { return type; }

    @Override
    public String toString() { return name; }  // used directly in JComboBox
}

