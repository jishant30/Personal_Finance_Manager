package com.personal_finance.personalfinance.ui.dialogs;

import com.personal_finance.personalfinance.model.Transaction;
import com.personal_finance.personalfinance.model.Category;
import com.personal_finance.personalfinance.dao.CategoryDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File	;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Modal dialog to add a new transaction. Returns the built Transaction via
 * getTransaction() after OK.
 */
public class AddTransactionDialog extends JDialog {

    private final JComboBox<String> typeCombo = new JComboBox<>(
                new String[]{"EXPENSE", "INCOME"});
    private final JComboBox<Category> categoryCombo = new JComboBox<>();
    private final JTextField amountField = new JTextField(12);
    private final JTextField dateField = new JTextField(12);   // yyyy-MM-dd
    private final JTextField noteField = new JTextField(20);
    private final JLabel receiptLabel = new JLabel("No file chosen");
    private final JButton receiptBtn = new JButton("Attach Receipt");
    private final JLabel statusLabel = new JLabel(" ");

    private final int userId;
    private byte[] receiptBytes = null;
    private Transaction result = null;

    private final CategoryDAO categoryDAO = new CategoryDAO();

    public AddTransactionDialog(Frame owner, int userId) {
        super(owner, "Add Transaction", true);
        this.userId = userId;
        buildUI();
        loadCategories("EXPENSE");
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, c, row++, "Type:", typeCombo);
        addRow(form, c, row++, "Category:", categoryCombo);
        addRow(form, c, row++, "Amount (₹):", amountField);

        dateField.setText(LocalDate.now().toString());
        addRow(form, c, row++, "Date (yyyy-MM-dd):", dateField);
        addRow(form, c, row++, "Note:", noteField);

        // receipt row
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Receipt:"), c);
        JPanel receiptRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        receiptRow.add(receiptBtn);
        receiptRow.add(receiptLabel);
        c.gridx = 1;
        form.add(receiptRow, c);

        root.add(form, BorderLayout.CENTER);

        // buttons
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        ok.setBackground(new Color(41, 128, 185));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.add(ok);
        btnRow.add(cancel);

        JPanel south = new JPanel(new BorderLayout(0, 4));
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        south.add(btnRow, BorderLayout.NORTH);
        south.add(statusLabel, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        add(root);
        setResizable(false);

        // reload categories when type changes
        typeCombo.addActionListener(e
                -> loadCategories((String) typeCombo.getSelectedItem()));

        receiptBtn.addActionListener(this::chooseReceipt);
        ok.addActionListener(this::onSave);
        cancel.addActionListener(e -> dispose());
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label,
            JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        p.add(field, c);
    }

    private void loadCategories(String type) {
        categoryCombo.removeAllItems();
        try {
            List<Category> cats = categoryDAO.getByType(type);
            for (Category cat : cats) {
                categoryCombo.addItem(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chooseReceipt(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                receiptBytes = Files.readAllBytes(file.toPath());
                receiptLabel.setText(file.getName());
            } catch (IOException ex) {
                statusLabel.setText("Could not read file.");
            }
        }
    }

    private void onSave(ActionEvent e) {
        String amountText = amountField.getText().trim();
        String dateText = dateField.getText().trim();
        String note = noteField.getText().trim();
        Category category = (Category) categoryCombo.getSelectedItem();

        if (amountText.isEmpty() || category == null) {
            statusLabel.setText("Amount and category are required.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            statusLabel.setText("Enter a valid positive amount.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            statusLabel.setText("Date must be yyyy-MM-dd.");
            return;
        }

        result = new Transaction.Builder(userId, category.getId(), amount, date)
                .categoryName(category.getName())
                .type(category.getType())
                .note(note)
                .receipt(receiptBytes)
                .build();

        dispose();
    }

    /**
     * Returns the built Transaction, or null if the dialog was cancelled.
     */
    public Transaction getTransaction() {
        return result;
    }
}
