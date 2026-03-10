package BillingSystem.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import BillingSystem.db.DBConnection;

public class BillingFrame {

    JFrame frame;

    public BillingFrame(int billId) {
        frame = new JFrame("Bill Receipt - Bill #" + billId);
        frame.setSize(650, 600);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ── Header ────────────────────────────────────────────────────────────
        JLabel shopLabel = new JLabel("BILLING SYSTEM", SwingConstants.CENTER);
        shopLabel.setFont(new Font("Arial", Font.BOLD, 22));
        shopLabel.setBounds(0, 15, 650, 30);
        frame.add(shopLabel);

        JLabel billLabel = new JLabel("Bill Receipt", SwingConstants.CENTER);
        billLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        billLabel.setBounds(0, 48, 650, 20);
        frame.add(billLabel);

        JSeparator sep1 = new JSeparator();
        sep1.setBounds(30, 75, 590, 2);
        frame.add(sep1);

        // ── Customer Info (loaded from DB) ────────────────────────────────────
        JLabel nameVal    = new JLabel("Loading...");
        JLabel phoneVal   = new JLabel("");
        JLabel addressVal = new JLabel("");
        JLabel billIdVal  = new JLabel("Bill #" + billId);
        JLabel dateVal    = new JLabel("");

        // Left side - customer
        JLabel nameKey    = new JLabel("Name:");
        JLabel phoneKey   = new JLabel("Phone:");
        JLabel addressKey = new JLabel("Address:");
        nameKey.setFont(new Font("Arial", Font.BOLD, 13));
        phoneKey.setFont(new Font("Arial", Font.BOLD, 13));
        addressKey.setFont(new Font("Arial", Font.BOLD, 13));

        nameKey.setBounds(30, 85, 80, 20);
        nameVal.setBounds(110, 85, 200, 20);
        phoneKey.setBounds(30, 108, 80, 20);
        phoneVal.setBounds(110, 108, 200, 20);
        addressKey.setBounds(30, 131, 80, 20);
        addressVal.setBounds(110, 131, 200, 20);

        frame.add(nameKey);    frame.add(nameVal);
        frame.add(phoneKey);   frame.add(phoneVal);
        frame.add(addressKey); frame.add(addressVal);

        // Right side - bill info
        JLabel billIdKey = new JLabel("Bill ID:");
        JLabel dateKey   = new JLabel("Date:");
        billIdKey.setFont(new Font("Arial", Font.BOLD, 13));
        dateKey.setFont(new Font("Arial", Font.BOLD, 13));

        billIdKey.setBounds(400, 85, 80, 20);
        billIdVal.setBounds(480, 85, 140, 20);
        dateKey.setBounds(400, 108, 80, 20);
        dateVal.setBounds(480, 108, 140, 20);

        frame.add(billIdKey); frame.add(billIdVal);
        frame.add(dateKey);   frame.add(dateVal);

        JSeparator sep2 = new JSeparator();
        sep2.setBounds(30, 160, 590, 2);
        frame.add(sep2);

        // ── Products Table ────────────────────────────────────────────────────
        String[] cols = {"SN", "Product Name", "Qty", "Price (Rs.)", "Total (Rs.)"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(60, 60, 60));
        table.getTableHeader().setForeground(Color.WHITE);

        // Right-align numeric columns
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 170, 590, 230);
        frame.add(scrollPane);

        // ── Grand Total ───────────────────────────────────────────────────────
        JSeparator sep3 = new JSeparator();
        sep3.setBounds(30, 408, 590, 2);
        frame.add(sep3);

        JLabel grandTotalKey = new JLabel("GRAND TOTAL:");
        grandTotalKey.setFont(new Font("Arial", Font.BOLD, 15));
        grandTotalKey.setBounds(370, 415, 140, 25);
        frame.add(grandTotalKey);

        JLabel grandTotalVal = new JLabel("Rs. 0.00");
        grandTotalVal.setFont(new Font("Arial", Font.BOLD, 15));
        grandTotalVal.setForeground(new Color(0, 128, 0));
        grandTotalVal.setBounds(510, 415, 120, 25);
        frame.add(grandTotalVal);

        // ── Thank you note ────────────────────────────────────────────────────
        JLabel thankYou = new JLabel("Thank you for your purchase!", SwingConstants.CENTER);
        thankYou.setFont(new Font("Arial", Font.ITALIC, 13));
        thankYou.setForeground(Color.GRAY);
        thankYou.setBounds(0, 450, 650, 20);
        frame.add(thankYou);

        // ── Close button ──────────────────────────────────────────────────────
        JButton closeButton = new JButton("Close");
        closeButton.setBounds(270, 480, 110, 32);
        closeButton.addActionListener(e -> frame.dispose());
        frame.add(closeButton);

        frame.setVisible(true);

        // ── Load data from DB ─────────────────────────────────────────────────
        loadBillFromDB(billId, nameVal, phoneVal, addressVal, dateVal, tableModel, grandTotalVal);
    }

    private void loadBillFromDB(int billId,
                                 JLabel nameVal, JLabel phoneVal, JLabel addressVal, JLabel dateVal,
                                 DefaultTableModel tableModel, JLabel grandTotalVal) {
        try {
            Connection conn = DBConnection.getConnection();

            // Fetch customer + bill info
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT c.name, c.phone, c.address, b.grand_total, b.created_at " +
                "FROM bills b JOIN customers c ON b.customer_id = c.id " +
                "WHERE b.id = ?");
            stmt.setInt(1, billId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameVal.setText(rs.getString("name"));
                phoneVal.setText(rs.getString("phone"));
                addressVal.setText(rs.getString("address"));
                grandTotalVal.setText("Rs. " + String.format("%.2f", rs.getDouble("grand_total")));
                dateVal.setText(rs.getTimestamp("created_at").toString().substring(0, 16));
            }

            // Fetch bill items
            PreparedStatement itemStmt = conn.prepareStatement(
                "SELECT product_name, quantity, price, total FROM bill_items WHERE bill_id = ?");
            itemStmt.setInt(1, billId);
            ResultSet items = itemStmt.executeQuery();

            int sn = 1;
            while (items.next()) {
                tableModel.addRow(new Object[]{
                    sn++,
                    items.getString("product_name"),
                    items.getInt("quantity"),
                    String.format("%.2f", items.getDouble("price")),
                    String.format("%.2f", items.getDouble("total"))
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                "Failed to load bill from database!\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}