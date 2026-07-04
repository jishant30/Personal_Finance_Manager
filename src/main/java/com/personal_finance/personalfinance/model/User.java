/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.dao;

/**
 *
 * @author Jisha
 */

public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final byte[] avatar;
    
    private User(Builder b) {
        this.id = b.id;
        this.username = b.username;
        this.passwordHash = b.passwordHash;
        this.fullName = b.fullName;
        this.avatar = b.avatar;
    }
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username=" + username + ", passwordHash=" + passwordHash + ", fullName=" + fullName + ", avatar=" + avatar + '}';
    }
    
    public static class Builder {
        // Required Fields
        private final String username;
        private final String passwordHash;
        
        // Optional Fields
        private int id = 0;
        private String fullName = "";
        private byte[] avatar = null;
        
        public Builder(String username, String passwordHash) {
            if(username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username is required!");
            }
            if(passwordHash == null || passwordHash.isBlank()) {
                throw new IllegalArgumentException("Password is required!");
            }
            this.username = username;
            this.passwordHash = passwordHash;
        }
        
        public Builder id(int id) {
            this.id = id;
            return this;
        }
        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        public Builder avatar(byte[] avatar) {
            this.avatar = avatar;
            return this;
        }
        
        public User build() {
            return new User(this);
        }
    }
}

