/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.dao;

/**
 *
 * @author Jisha
 */
import com.personal_finance.personalfinance.db.DBConnection;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.personal_finance.personalfinance.model.Category;

public class CategoryDAO {

    private Connection conn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    public List<Category> getAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, type FROM categories ORDER BY type, name";
        try (
                PreparedStatement ps = conn().prepareStatement(sql); 
                ResultSet rs = ps.executeQuery()
            ) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.
                        getString("type")));
            }
        }
        return list;
    }

    public List<Category> getByType(String type) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, type FROM categories WHERE type = ? ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Category(rs.getInt("id"), rs.getString("name"),
                            rs.getString("type")) {});
                }
            }
        }
        return list;
    }
}

