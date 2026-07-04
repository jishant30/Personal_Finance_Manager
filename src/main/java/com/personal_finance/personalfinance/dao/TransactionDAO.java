/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.dao;

import com.personal_finance.personalfinance.db.DBConnection;
import com.personal_finance.personalfinance.model.Transaction;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jisha
 */

public class TransactionDAO {
    private Connection conn() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    public void insert(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, note, receipt, txn_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt   (1, t.getUserId());
            ps.setInt   (2, t.getCategoryId());
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getNote());
            ps.setBytes (5, t.getReceipt());
            ps.setDate  (6, Date.valueOf(t.getDate()));
            ps.executeUpdate();
        }
    }

    public void delete(int transactionId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }

    /** All transactions for a user, newest first. */
    public List<Transaction> getByUser(int userId) throws SQLException {
        String sql = "SELECT t.id, t.user_id, t.category_id, c.name AS cat_name, c.type, " +
                     "       t.amount, t.note, t.receipt, t.txn_date " +
                     "FROM transactions t " +
                     "JOIN categories c ON c.id = t.category_id " +
                     "WHERE t.user_id = ? " +
                     "ORDER BY t.txn_date DESC";
        return query(sql, userId);
    }

    /** Transactions for a user filtered by month (1-12) and year. */
    public List<Transaction> getByMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT t.id, t.user_id, t.category_id, c.name AS cat_name, c.type, " +
                     "       t.amount, t.note, t.receipt, t.txn_date " +
                     "FROM transactions t " +
                     "JOIN categories c ON c.id = t.category_id " +
                     "WHERE t.user_id = ? AND MONTH(t.txn_date) = ? AND YEAR(t.txn_date) = ? " +
                     "ORDER BY t.txn_date DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** category name → total amount, for a given month/year and type (INCOME/EXPENSE). */
    public Map<String, Double> getSummaryByCategory(int userId, int month, int year, String type)
            throws SQLException {
        String sql = "SELECT c.name, SUM(t.amount) AS total " +
                     "FROM transactions t " +
                     "JOIN categories c ON c.id = t.category_id " +
                     "WHERE t.user_id = ? AND MONTH(t.txn_date) = ? AND YEAR(t.txn_date) = ? " +
                     "  AND c.type = ? " +
                     "GROUP BY c.name ORDER BY total DESC";
        Map<String, Double> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt   (1, userId);
            ps.setInt   (2, month);
            ps.setInt   (3, year);
            ps.setString(4, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("name"), rs.getDouble("total"));
            }
        }
        return map;
    }

    /** Total income and expense for a given month/year — returns double[]{income, expense}. */
    public double[] getMonthTotals(int userId, int month, int year) throws SQLException {
        String sql = "SELECT c.type, SUM(t.amount) AS total " +
                     "FROM transactions t " +
                     "JOIN categories c ON c.id = t.category_id " +
                     "WHERE t.user_id = ? AND MONTH(t.txn_date) = ? AND YEAR(t.txn_date) = ? " +
                     "GROUP BY c.type";
        double income = 0, expense = 0;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("INCOME".equals(rs.getString("type")))  income  = rs.getDouble("total");
                    else                                         expense = rs.getDouble("total");
                }
            }
        }
        return new double[]{income, expense};
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private List<Transaction> query(String sql, int userId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Transaction map(ResultSet rs) throws SQLException {
        return new Transaction.Builder(
                rs.getInt("user_id"),
                rs.getInt("category_id"),
                rs.getDouble("amount"),
                rs.getDate("txn_date").toLocalDate())
                .id(rs.getInt("id"))
                .categoryName(rs.getString("cat_name"))
                .type(rs.getString("type"))
                .note(rs.getString("note"))
                .receipt(rs.getBytes("receipt"))
                .build();
    }
}

