package BillingSystem.ui;

import BillingSystem.db.DBConnection;
import BillingSystem.model.Customer;
import BillingSystem.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductForm {

    JFrame frame;
    Customer customer;

    JTable table;
    DefaultTableModel model;

    JLabel titleLabel;
    JLabel totalLabel;

    JTextField totalField;

    JButton addButton;
    JButton editButton;
    JButton deleteButton;
    JButton backButton;
    JButton submitButton;

    List<Product> products = new ArrayList<>();

    public ProductForm(Customer customer) {
        this.customer = customer;

        frame = new JFrame("Product Details");
        frame.setSize(700, 550);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // Title
        titleLabel = new JLabel("Fill Product Details", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(200, 20, 300, 30);
        frame.add(titleLabel);

        // Table
        String columns[] = {"SN", "Product Name", "Quantity", "Price", "Total"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 4;
            }
        };
        table = new JTable(model);
        table.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 2 || col == 3) { // Quantity or Price changed
                calculateRowTotal(row);
                calculateGrandTotal();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(50, 70, 600, 250);
        frame.add(scroll);

        // Top buttons
        addButton = new JButton("Add");
        addButton.setBounds(50, 340, 80, 30);
        frame.add(addButton);

        editButton = new JButton("Edit");
        editButton.setBounds(150, 340, 80, 30);
        frame.add(editButton);

        deleteButton = new JButton("Delete");
        deleteButton.setBounds(250, 340, 90, 30);
        frame.add(deleteButton);

        // Grand total label & field
        totalLabel = new JLabel("Grand Total:");
        totalLabel.setBounds(360, 340, 100, 30);
        frame.add(totalLabel);

        totalField = new JTextField("0.00");
        totalField.setEditable(false);
        totalField.setBounds(460, 340, 190, 30);
        frame.add(totalField);

        // Bottom buttons
        backButton = new JButton("Back");
        backButton.setBounds(360, 430, 100, 35);
        frame.add(backButton);

        submitButton = new JButton("Submit");
        submitButton.setBounds(480, 430, 120, 35);
        frame.add(submitButton);

        // Button actions
        addButton.addActionListener(e -> addRow());
        editButton.addActionListener(e -> editRow());
        deleteButton.addActionListener(e -> deleteRow());
        backButton.addActionListener(e -> goBack());
        submitButton.addActionListener(e -> submitProducts());

        frame.setVisible(true);
    }

    // Add new row
    private void addRow() {
        int sn = model.getRowCount() + 1;
        model.addRow(new Object[]{sn, "", "", "", "0.00"});
    }

    // Edit selected row
   private void editRow() {
    int row = table.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(frame, "Please select a row to edit!");
        return;
    }

    // Select the row
    table.setRowSelectionInterval(row, row);

    // Start editing the first editable column (Product Name)
    table.editCellAt(row, 1);

    // Request focus for the editor component so user can type immediately
    table.getEditorComponent().requestFocus();
}

    // Delete selected row
    private void deleteRow() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a row first!");
            return;
        }
        String sn = model.getValueAt(row, 0).toString();
        String productName = model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete?\nRow: " + sn +
                        "\nProduct: " + (productName.isEmpty() ? "(empty)" : productName),
                "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (confirm == JOptionPane.OK_OPTION) {
            model.removeRow(row);
            // Reset serial numbers
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0);
            }
            calculateGrandTotal();
        }
    }

    // Go back to customer form
    private void goBack() {
        new CustomerForm(customer);
        frame.dispose();
    }

    // Calculate total for a row
    private void calculateRowTotal(int row) {
        try {
            double qty = Double.parseDouble(model.getValueAt(row, 2).toString());
            double price = Double.parseDouble(model.getValueAt(row, 3).toString());
            double total = qty * price;
            model.setValueAt(String.format("%.2f", total), row, 4);
        } catch (Exception e) {
            model.setValueAt("0.00", row, 4);
        }
    }

    // Calculate grand total
    private void calculateGrandTotal() {
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                total += Double.parseDouble(model.getValueAt(i, 4).toString());
            } catch (Exception ignored) {}
        }
        totalField.setText(String.format("%.2f", total));
    }

    // Validate all rows
    private boolean validateRows() {
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = model.getValueAt(i, 1).toString().trim();
            String qty = model.getValueAt(i, 2).toString().trim();
            String price = model.getValueAt(i, 3).toString().trim();

            if (name.isEmpty() || qty.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Row " + (i + 1) + " has empty fields!");
                return false;
            }

            try {
                int q = Integer.parseInt(qty);
                double p = Double.parseDouble(price);
                if (q <= 0 || p <= 0) {
                    JOptionPane.showMessageDialog(frame, "Row " + (i + 1) + " quantity/price must be > 0");
                    return false;
                }
                calculateRowTotal(i);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Row " + (i + 1) + " has invalid numbers");
                return false;
            }
        }
        calculateGrandTotal();
        return true;
    }

    // Submit products to DB
    private void submitProducts() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Please add at least one product!");
            return;
        }
        if (!validateRows()) return;

        products.clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = model.getValueAt(i, 1).toString();
            int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
            double price = Double.parseDouble(model.getValueAt(i, 3).toString());
            products.add(new Product(name, qty, price));
        }

        double grandTotal = Double.parseDouble(totalField.getText());
        int billId = saveToDatabase(grandTotal);

        if (billId == -1) {
            JOptionPane.showMessageDialog(frame, "Database Error!");
        } else {
            int option = JOptionPane.showConfirmDialog(frame, "Saved Successfully!\nGenerate Bill?",
                    "Success", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                new BillingFrame(billId);
            }
            frame.dispose();
        }
    }

    // Save to database
    private int saveToDatabase(double total) {
        try {
            Connection conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Customer
            PreparedStatement psCustomer = conn.prepareStatement(
                    "INSERT INTO customers(name,phone,address) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psCustomer.setString(1, customer.getName());
            psCustomer.setString(2, customer.getPhone());
            psCustomer.setString(3, customer.getAddress());
            psCustomer.executeUpdate();
            ResultSet rs = psCustomer.getGeneratedKeys();
            rs.next();
            int customerId = rs.getInt(1);

            // Bill
            PreparedStatement psBill = conn.prepareStatement(
                    "INSERT INTO bills(customer_id,grand_total) VALUES(?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psBill.setInt(1, customerId);
            psBill.setDouble(2, total);
            psBill.executeUpdate();
            ResultSet rs2 = psBill.getGeneratedKeys();
            rs2.next();
            int billId = rs2.getInt(1);

            // Bill items
            PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO bill_items(bill_id,product_name,quantity,price,total) VALUES(?,?,?,?,?)");
            for (Product p : products) {
                psItem.setInt(1, billId);
                psItem.setString(2, p.getName());
                psItem.setInt(3, p.getQuantity());
                psItem.setDouble(4, p.getPrice());
                psItem.setDouble(5, p.getTotal());
                psItem.executeUpdate();
            }

            conn.commit();
            return billId;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }
}