/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.util;

/**
 *
 * @author Jisha
 */


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PasswordUtil {
    public static String hash(String plainText) throws RuntimeException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainText.getBytes());
            StringBuilder hex = new StringBuilder(64);
            
            // Convert the hash to human friendly hex codes.
            for(byte b: bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm Not Available!");
        }
    }
    
    public static boolean matches(String plainText, String securedHash) {
        return hash(plainText).equals(securedHash);
    }
}
