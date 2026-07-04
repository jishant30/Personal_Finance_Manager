/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.personal_finance.personalfinance.ui;

/**
 *
 * @author Jisha
 */

import com.personal_finance.personalfinance.dao.TransactionDAO;
import com.personal_finance.personalfinance.dao.User;
import com.personal_finance.personalfinance.dao.UserDAO;
import com.personal_finance.personalfinance.model.Transaction;
import com.personal_finance.personalfinance.ui.charts.BarChartPanel;
import com.personal_finance.personalfinance.ui.charts.PieChartPanel;
import com.personal_finance.personalfinance.ui.dialogs.AddTransactionDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

/**
 * Main application window shown after successful login.
 *
 * Layout: NORTH — top bar (greeting, avatar, logout) CENTER — JTabbedPane Tab
 * 1: Dashboard (summary cards + pie chart) Tab 2: Transactions table Tab 3: Bar
 * chart (by category)
 */
public class MainFrame extends JFrame {

    private final User user;
    private final TransactionDAO txDAO = new TransactionDAO();

    // month/year filter (shared across tabs)
    private final JComboBox<String> monthCombo;
    private final JComboBox<Integer> yearCombo;

    // Dashboard tab
    private final JLabel incomeLabel = new JLabel("₹0.00");
    private final JLabel expenseLabel = new JLabel("₹0.00");
    private final JLabel balanceLabel = new JLabel("₹0.00");
    private final PieChartPanel pieChart = new PieChartPanel();

    // Transactions tab
    private final DefaultTableModel tableModel;
    private final JTable txTable;

    // Bar chart tab
    private final BarChartPanel barChart = new BarChartPanel();
    private final JComboBox<String> barTypeCombo = new JComboBox<>(
                new String[]{"EXPENSE", "INCOME"});

    // Avatar
    private final JLabel avatarLabel = new JLabel();

    public MainFrame(User user) {
        this.user = user;
        setTitle("Personal Finance Tracker — " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));

        // init month/year combos
        String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        yearCombo = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 3; y <= currentYear; y++) {
            yearCombo.addItem(y);
        }
        yearCombo.setSelectedItem(currentYear);

        // table
        String[] cols = {"#", "Date", "Type", "Category", "Amount (₹)", "Note"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        txTable = new JTable(tableModel);
        txTable.setRowHeight(24);
        txTable.getColumnModel().getColumn(0).setMaxWidth(40);
        txTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        buildUI();
        pack();
        setLocationRelativeTo(null);
        refresh();
    }

    // ------------------------------------------------------------------
    // UI construction
    // ------------------------------------------------------------------
    private void buildUI() {
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(41, 128, 185));
        bar.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        // left: avatar + name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        refreshAvatar();
        left.add(avatarLabel);

        JLabel nameLabel = new JLabel("Hello, " + user.getFullName() + "!");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        left.add(nameLabel);

        // right: filter + upload avatar + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JLabel filterLabel = new JLabel("Showing:");
        filterLabel.setForeground(Color.WHITE);
        right.add(filterLabel);
        right.add(monthCombo);
        right.add(yearCombo);

        JButton avatarBtn = new JButton("Upload Photo");
        avatarBtn.addActionListener(this::uploadAvatar);
        right.add(avatarBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setOpaque(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        right.add(logoutBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        // refresh when filter changes
        monthCombo.addActionListener(e -> refresh());
        yearCombo.addActionListener(e -> refresh());

        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", buildDashboardTab());
        tabs.addTab("Transactions", buildTransactionsTab());
        tabs.addTab("Bar Chart", buildBarChartTab());
        return tabs;
    }

    // --- Dashboard tab ---
    private JPanel buildDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // summary cards at top
        panel.add(buildSummaryCards(), BorderLayout.NORTH);

        // pie chart in centre
        pieChart.setBorder(BorderFactory.createTitledBorder(
                "Expenses by Category"));
        panel.add(pieChart, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));

        cards.add(makeCard("Total Income", incomeLabel, new Color(39, 174, 96)));
        cards.add(
                makeCard("Total Expense", expenseLabel, new Color(231, 76, 60)));
        cards.add(makeCard("Balance", balanceLabel, new Color(41, 128, 185)));

        for (JLabel lbl : new JLabel[]{incomeLabel, expenseLabel, balanceLabel}) {
            lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
            lbl.setForeground(Color.WHITE);
        }
        return cards;
    }

    private JPanel makeCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setForeground(new Color(220, 240, 255));
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // --- Transactions tab ---
    private JPanel buildTransactionsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton addBtn = new JButton("+ Add Transaction");
        addBtn.setBackground(new Color(39, 174, 96));
        addBtn.setForeground(Color.WHITE);
        addBtn.setOpaque(true);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(this::onAddTransaction);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setOpaque(true);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(this::onDeleteTransaction);

        JButton viewReceiptBtn = new JButton("View Receipt");
        viewReceiptBtn.addActionListener(this::onViewReceipt);

        btnRow.add(addBtn);
        btnRow.add(deleteBtn);
        btnRow.add(viewReceiptBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    // --- Bar chart tab ---
    private JPanel buildBarChartTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Show:"));
        controls.add(barTypeCombo);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshBarChart());
        controls.add(refreshBtn);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(barChart, BorderLayout.CENTER);
        barTypeCombo.addActionListener(e -> refreshBarChart());
        return panel;
    }

    // ------------------------------------------------------------------
    // Data refresh
    // ------------------------------------------------------------------
    private void refresh() {
        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();

        try {
            double[] totals = txDAO.getMonthTotals(user.getId(), month, year);
            double income = totals[0];
            double expense = totals[1];
            double balance = income - expense;

            incomeLabel.setText(String.format("₹%.2f", income));
            expenseLabel.setText(String.format("₹%.2f", expense));
            balanceLabel.setText(String.format("₹%.2f", balance));
            balanceLabel.setForeground(balance >= 0 ? Color.WHITE : new Color(
                    255, 200, 200));

            // pie chart — expense breakdown
            Map<String, Double> expMap = txDAO.
                    getSummaryByCategory(user.getId(), month, year, "EXPENSE");
            pieChart.setData(expMap,
                    Month.of(month).name() + " " + year + " — Expenses");

            // transactions table
            List<Transaction> txList = txDAO.getByMonth(user.getId(), month,
                    year);
            tableModel.setRowCount(0);
            int rowNum = 1;
            for (Transaction t : txList) {
                tableModel.addRow(new Object[]{
                    rowNum++,
                    t.getDate(),
                    t.getType(),
                    t.getCategoryName(),
                    String.format("%.2f", t.getAmount()),
                    t.getNote()
                });
            }

            refreshBarChart();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.
                    getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshBarChart() {
        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();
        String type = (String) barTypeCombo.getSelectedItem();
        try {
            Map<String, Double> data = txDAO.getSummaryByCategory(user.getId(),
                    month, year, type);
            barChart.setData(data, type + " by Category — " + Month.of(month).
                    name() + " " + year,
                    "INCOME".equals(type));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------
    private void onAddTransaction(ActionEvent e) {
        AddTransactionDialog dlg = new AddTransactionDialog(this, user.getId());
        dlg.setVisible(true);
        Transaction t = dlg.getTransaction();
        if (t != null) {
            try {
                txDAO.insert(t);
                refresh();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Could not save: " + ex.
                        getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteTransaction(ActionEvent e) {
        int row = txTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this transaction?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // We need the real DB id — re-query by month to get ordered list
        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();
        try {
            List<Transaction> txList = txDAO.getByMonth(user.getId(), month,
                    year);
            txDAO.delete(txList.get(row).getId());
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.
                    getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onViewReceipt(ActionEvent e) {
        int row = txTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction first.");
            return;
        }

        int month = monthCombo.getSelectedIndex() + 1;
        int year = (Integer) yearCombo.getSelectedItem();
        try {
            List<Transaction> txList = txDAO.getByMonth(user.getId(), month,
                    year);
            byte[] bytes = txList.get(row).getReceipt();
            if (bytes == null || bytes.length == 0) {
                JOptionPane.showMessageDialog(this,
                        "No receipt attached to this transaction.");
                return;
            }
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) {
                JOptionPane.showMessageDialog(this, "Cannot read image.");
                return;
            }

            JLabel imgLabel = new JLabel(new ImageIcon(
                    img.getScaledInstance(400, -1, Image.SCALE_SMOOTH)));
            JOptionPane.showMessageDialog(this, imgLabel, "Receipt",
                    JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void uploadAvatar(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            new UserDAO().updateAvatar(user.getId(), bytes);
            // update avatar display
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img != null) {
                Image scaled = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not upload photo: " + ex.
                    getMessage());
        }
    }

    private void refreshAvatar() {
        byte[] bytes = user.getAvatar();
        if (bytes != null && bytes.length > 0) {
            try {
                BufferedImage img = ImageIO.
                        read(new ByteArrayInputStream(bytes));
                if (img != null) {
                    avatarLabel.setIcon(new ImageIcon(img.getScaledInstance(40,
                            40, Image.SCALE_SMOOTH)));
                    return;
                }
            } catch (IOException ignored) {
            }
        }
        // default placeholder
        avatarLabel.setIcon(null);
        avatarLabel.setText("👤");
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
    }
}

