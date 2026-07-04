/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Jisha
 */
public class DBConnection {

    private static final String connectionURL = "jdbc:mysql://localhost:3306/expense_tracker_tsec";
    private static final String username = "root";
    private static final String password = "abcd1234";
    
    private Connection connection = null;
    
    private static volatile DBConnection instance = null;
    
    private DBConnection() throws SQLException {
        connection = DriverManager.getConnection(connectionURL, username, password);
    }
    public static DBConnection getInstance() throws SQLException {
        
        if(instance == null) {
            synchronized(DBConnection.class) {
                if(instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        if(connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(connectionURL, username, password);
        }
        return connection;
    }
    
    public void close() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    public static void main(String[] args) {
        try {
            DBConnection dbConnection = DBConnection.getInstance();
            Connection conn = dbConnection.getConnection();
            if(conn != null) {
                JOptionPane.showMessageDialog(null, "Connection Establised");
            } else {
                JOptionPane.showMessageDialog(null, "Error...");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
}
