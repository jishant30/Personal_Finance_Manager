/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.dao;

import com.personal_finance.personalfinance.db.DBConnection;
import com.personal_finance.personalfinance.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Jisha
 */

public class UserDAO {

    private Connection conn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    public User register(String username, String password, String fullName) throws
            SQLException {
        String sql = "INSERT INTO users(username, password_hash, full_name) "
                + "VALUES (?, ?, ?);";

        try (PreparedStatement ps = conn().prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName);

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return new User.Builder(username, PasswordUtil.hash(password))
                        .id(rs.getInt(1))
                        .fullName(fullName)
                        .build();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                String storedHash = rs.getString("password_hash");
                
                if(PasswordUtil.matches(password, storedHash)) {
                    return new User.Builder(rs.getString("username"), storedHash)
                            .id(rs.getInt("id"))
                            .fullName(rs.getString("full_name"))
                            .avatar(rs.getBytes("avatar"))
                            .build();
                }
            }
        }
        return null;
    }
    
    /** Save a profile photo for the user. */
    public void updateAvatar(int userId, byte[] imageBytes) throws SQLException {
        String sql = "UPDATE users SET avatar = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBytes(1, imageBytes);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public static void main(String args[]) throws Exception {
        UserDAO userDAO = new UserDAO();

        User u1 = userDAO.register("krish4", "4321dcba", "Krish Mulwani");
        System.out.println(u1);
        User u2 = userDAO.register("Jishant", "jishant1234", "Jishant Santwani");
        System.out.println(u2);
    }
}

