package BillingSystem.ui;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import BillingSystem.db.DBConnection;
import BillingSystem.model.Customer;
import BillingSystem.model.Product;

public class ProductForm {
    JFrame frame;
    Customer customer;
    List<Product> products = new ArrayList<>();

    JLabel titleLabel, totalLabel;
    JTextField totalField;
    JTable productTable;
    DefaultTableModel tableModel;
    JButton addButton, editButton, deleteButton, submitButton;

    public ProductForm(Customer customer) {
        this.customer = customer;

        frame = new JFrame("Product Form");
        frame.setSize(700, 560);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        titleLabel = new JLabel("Fill Out the Product Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBounds(220, 10, 350, 30);
        frame.add(titleLabel);

        // Table setup
        String[] columns = {"SN", "Product Name", "Quantity", "Price", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 4; // SN and Total not editable
            }
        };

        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBounds(50, 50, 600, 270);
        frame.add(scrollPane);

        // Auto-calculate row total when qty/price changes
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int col = e.getColumn();
                if (col == 2 || col == 3) updateRowTotal(e.getFirstRow());
            }
            updateGrandTotal();
        });

        // Buttons
        addButton    = new JButton("Add");
        editButton   = new JButton("Edit");
        deleteButton = new JButton("Delete");
        submitButton = new JButton("Submit");

        addButton.setBounds(50, 340, 80, 30);
        editButton.setBounds(150, 340, 80, 30);
        deleteButton.setBounds(250, 340, 90, 30);
        submitButton.setBounds(530, 430, 120, 35);

        frame.add(addButton);
        frame.add(editButton);
        frame.add(deleteButton);
        frame.add(submitButton);

        // Grand Total
        totalLabel = new JLabel("Grand Total:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setBounds(380, 340, 110, 30);
        frame.add(totalLabel);

        totalField = new JTextField("0.00");
        totalField.setFont(new Font("Arial", Font.BOLD, 14));
        totalField.setEditable(false);
        totalField.setBackground(new Color(230, 255, 230));
        totalField.setHorizontalAlignment(JTextField.RIGHT);
        totalField.setBounds(490, 340, 160, 30);
        frame.add(totalField);

        addButton.addActionListener(e -> addNewRow());
        editButton.addActionListener(e -> editSelectedRow());
        deleteButton.addActionListener(e -> deleteSelectedRow());
        submitButton.addActionListener(e -> submitProducts());

        frame.setVisible(true);
    }

    private void updateRowTotal(int row) {
        try {
            double qty   = Double.parseDouble(tableModel.getValueAt(row, 2).toString().trim());
            double price = Double.parseDouble(tableModel.getValueAt(row, 3).toString().trim());
            tableModel.setValueAt(String.format("%.2f", qty * price), row, 4);
        } catch (NumberFormatException ex) {
            tableModel.setValueAt("0.00", row, 4);
        }
    }

    private void updateGrandTotal() {
        double grand = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try { grand += Double.parseDouble(tableModel.getValueAt(i, 4).toString()); }
            catch (NumberFormatException ignored) {}
        }
        totalField.setText(String.format("%.2f", grand));
    }

    private void addNewRow() {
        if (productTable.isEditing()) productTable.getCellEditor().stopCellEditing();
        if (!validateAllRows()) return;

        int sn = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{sn, "", "", "", "0.00"});
        int newRow = tableModel.getRowCount() - 1;
        productTable.setRowSelectionInterval(newRow, newRow);
        productTable.editCellAt(newRow, 1);
        productTable.requestFocus();
    }

    private void editSelectedRow() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a row to edit!", "No Row Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        productTable.editCellAt(row, 1);
        productTable.setRowSelectionInterval(row, row);
        productTable.requestFocus();
    }

    private void deleteSelectedRow() {
        if (productTable.isEditing()) productTable.getCellEditor().stopCellEditing();
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a row to delete!", "No Row Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = tableModel.getValueAt(row, 1).toString().trim();
        int choice = JOptionPane.showConfirmDialog(frame,
                "Delete this row?\nSN: " + tableModel.getValueAt(row, 0) + "\nProduct: " + (name.isEmpty() ? "(empty)" : name),
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
            for (int i = 0; i < tableModel.getRowCount(); i++) tableModel.setValueAt(i + 1, i, 0);
            updateGrandTotal();
        }
    }

    // ─── SUBMIT: save to DB, show popup, offer Generate Bill ──────────────────
    private void submitProducts() {
        if (productTable.isEditing()) productTable.getCellEditor().stopCellEditing();

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Please add at least one product!", "No Products", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateAllRows()) return;

        // Build product list
        products.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name  = tableModel.getValueAt(i, 1).toString().trim();
            int qty      = Integer.parseInt(tableModel.getValueAt(i, 2).toString().trim());
            double price = Double.parseDouble(tableModel.getValueAt(i, 3).toString().trim());
            products.add(new Product(name, qty, price));
        }
        double grandTotal = Double.parseDouble(totalField.getText().trim());

        // Save to database and get bill ID
        int billId = saveToDatabase(grandTotal);
        if (billId == -1) {
            JOptionPane.showMessageDialog(frame, "Failed to save to database!\nCheck your DB connection.", "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Success popup with Generate Bill option
        int choice = JOptionPane.showOptionDialog(frame,
                "Data saved successfully!\nBill ID: " + billId + "\n\nDo you want to generate the bill?",
                "Saved",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Generate Bill", "Close"},
                "Generate Bill");

        if (choice == 0) {
            new BillingFrame(billId); // open bill using the saved bill ID
        }

        frame.dispose();
    }

    // ─── Insert customer → bill → bill_items into DB ──────────────────────────
    private int saveToDatabase(double grandTotal) {
        try {
            Connection conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert customer
            PreparedStatement csStmt = conn.prepareStatement(
                "INSERT INTO customers (name, phone, address) VALUES (?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
            csStmt.setString(1, customer.getName());
            csStmt.setString(2, customer.getPhone());
            csStmt.setString(3, customer.getAddress());
            csStmt.executeUpdate();
            ResultSet csKeys = csStmt.getGeneratedKeys();
            int customerId = csKeys.next() ? csKeys.getInt(1) : -1;

            // 2. Insert bill
            PreparedStatement billStmt = conn.prepareStatement(
                "INSERT INTO bills (customer_id, grand_total) VALUES (?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
            billStmt.setInt(1, customerId);
            billStmt.setDouble(2, grandTotal);
            billStmt.executeUpdate();
            ResultSet billKeys = billStmt.getGeneratedKeys();
            int billId = billKeys.next() ? billKeys.getInt(1) : -1;

            // 3. Insert each product
            PreparedStatement itemStmt = conn.prepareStatement(
                "INSERT INTO bill_items (bill_id, product_name, quantity, price, total) VALUES (?, ?, ?, ?, ?)");
            for (Product p : products) {
                itemStmt.setInt(1, billId);
                itemStmt.setString(2, p.getName());
                itemStmt.setInt(3, p.getQuantity());
                itemStmt.setDouble(4, p.getPrice());
                itemStmt.setDouble(5, p.getTotal());
                itemStmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return billId;

        } catch (Exception e) {
            System.err.println("DB Error: " + e.getMessage());
            return -1;
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────
    private boolean validateAllRows() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int rowNum = i + 1;

            String name = tableModel.getValueAt(i, 1).toString().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Row " + rowNum + ": Product Name cannot be empty!", "Missing Field", JOptionPane.WARNING_MESSAGE);
                focusCell(i, 1); return false;
            }

            String qtyStr = tableModel.getValueAt(i, 2).toString().trim();
            if (qtyStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Row " + rowNum + ": Quantity cannot be empty!", "Missing Field", JOptionPane.WARNING_MESSAGE);
                focusCell(i, 2); return false;
            }
            int qty;
            try { qty = Integer.parseInt(qtyStr); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Row " + rowNum + ": Quantity must be a whole number!", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                focusCell(i, 2); return false;
            }
            if (qty <= 0) {
                JOptionPane.showMessageDialog(frame, "Row " + rowNum + ": Quantity must be greater than 0!", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                focusCell(i, 2); return false;
            }

            String priceStr = tableModel.getValueAt(i, 3).toString().trim();
            if (priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Row " + rowNum + ": Price cannot be empty!", "Missing Field", JOptionPane.WARNING_MESSAGE);
                focusCell(i, 3); return false;
            }
            double price;
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Row " + rowNum + ": Price must be a valid number!", "Invalid Price", JOptionPane.WARNING_MESSAGE);
                focusCell(i, 3); return false;
            }
            if (price <= 0) {
                JOptionPane.showMessageDialog(frame, "Row " + rowNum + ": Price must be greater than 0!", "Invalid Price", JOptionPane.WARNING_MESSAGE);
                focusCell(i, 3); return false;
            }
        }
        return true;
    }

    private void focusCell(int row, int col) {
        productTable.setRowSelectionInterval(row, row);
        productTable.editCellAt(row, col);
        productTable.requestFocus();
    }
}