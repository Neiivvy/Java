package BillingSystem.ui;

import BillingSystem.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;

public class BillingFrame {

    JFrame frame;

    public BillingFrame(int billId) {
        frame = new JFrame("Bill Receipt - Bill #" + billId);
        frame.setSize(650, 600);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ── Header ──
        JLabel shopLabel = new JLabel("BILLING SYSTEM", SwingConstants.CENTER);
        shopLabel.setFont(new Font("Arial", Font.BOLD, 22));
        shopLabel.setBounds(0, 15, 650, 30);
        frame.add(shopLabel);

        JLabel billLabel = new JLabel("Bill Receipt", SwingConstants.CENTER);
        billLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        billLabel.setBounds(0, 50, 650, 20);
        frame.add(billLabel);

        JSeparator sep1 = new JSeparator();
        sep1.setBounds(30, 75, 590, 2);
        frame.add(sep1);

        // ── Customer Info ──
        JLabel nameVal = new JLabel();
        JLabel phoneVal = new JLabel();
        JLabel addressVal = new JLabel();
        JLabel billIdVal = new JLabel("Bill #" + billId);
        JLabel dateVal = new JLabel();

        JLabel nameKey = new JLabel("Name:");
        JLabel phoneKey = new JLabel("Phone:");
        JLabel addressKey = new JLabel("Address:");
        JLabel billIdKey = new JLabel("Bill ID:");
        JLabel dateKey = new JLabel("Date:");

        Font keyFont = new Font("Arial", Font.BOLD, 13);
        nameKey.setFont(keyFont); phoneKey.setFont(keyFont); addressKey.setFont(keyFont);
        billIdKey.setFont(keyFont); dateKey.setFont(keyFont);

        nameKey.setBounds(30, 85, 80, 20); nameVal.setBounds(110, 85, 200, 20);
        phoneKey.setBounds(30, 108, 80, 20); phoneVal.setBounds(110, 108, 200, 20);
        addressKey.setBounds(30, 131, 80, 20); addressVal.setBounds(110, 131, 200, 20);
        billIdKey.setBounds(400, 85, 80, 20); billIdVal.setBounds(480, 85, 140, 20);
        dateKey.setBounds(400, 108, 80, 20); dateVal.setBounds(480, 108, 140, 20);

        frame.add(nameKey); frame.add(nameVal);
        frame.add(phoneKey); frame.add(phoneVal);
        frame.add(addressKey); frame.add(addressVal);
        frame.add(billIdKey); frame.add(billIdVal);
        frame.add(dateKey); frame.add(dateVal);

        JSeparator sep2 = new JSeparator();
        sep2.setBounds(30, 160, 590, 2);
        frame.add(sep2);

        // ── Products Table ──
        String[] columns = {"SN", "Product Name", "Qty", "Price (Rs.)", "Total (Rs.)"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 170, 590, 230);
        frame.add(scrollPane);

        // ── Grand Total ──
        JSeparator sep3 = new JSeparator();
        sep3.setBounds(30, 408, 590, 2);
        frame.add(sep3);

        JLabel grandTotalKey = new JLabel("GRAND TOTAL:");
        grandTotalKey.setFont(new Font("Arial", Font.BOLD, 15));
        grandTotalKey.setBounds(370, 415, 140, 25);
        frame.add(grandTotalKey);

        JLabel grandTotalVal = new JLabel("Rs. 0.00");
        grandTotalVal.setFont(new Font("Arial", Font.BOLD, 15));
        grandTotalVal.setForeground(new Color(0,128,0));
        grandTotalVal.setBounds(510, 415, 120, 25);
        frame.add(grandTotalVal);

        // ── Thank You Note ──
        JLabel thankYou = new JLabel("Thank you for your purchase!", SwingConstants.CENTER);
        thankYou.setFont(new Font("Arial", Font.ITALIC, 13));
        thankYou.setForeground(Color.GRAY);
        thankYou.setBounds(0, 450, 650, 20);
        frame.add(thankYou);

        // ── Close & Print Buttons ──
        JButton closeButton = new JButton("Close");
        closeButton.setBounds(180, 480, 110, 32);
        closeButton.addActionListener(e -> frame.dispose());
        frame.add(closeButton);

        JButton printButton = new JButton("Print Bill");
        printButton.setBounds(320, 480, 110, 32);
        printButton.addActionListener(e -> printBill(table));
        frame.add(printButton);

        frame.setVisible(true);

        // ── Load data from DB ──
        loadBill(billId, nameVal, phoneVal, addressVal, dateVal, tableModel, grandTotalVal);
    }

    private void loadBill(int billId,
                          JLabel nameVal, JLabel phoneVal, JLabel addressVal, JLabel dateVal,
                          DefaultTableModel tableModel, JLabel grandTotalVal) {
        try {
            Connection conn = DBConnection.getConnection();

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
                dateVal.setText(rs.getTimestamp("created_at").toString().substring(0,16));
            }

            PreparedStatement itemsStmt = conn.prepareStatement(
                "SELECT product_name, quantity, price, total FROM bill_items WHERE bill_id = ?");
            itemsStmt.setInt(1, billId);
            ResultSet items = itemsStmt.executeQuery();

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
                "Failed to load bill!\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printBill(JTable table) {
        try {
            boolean printed = table.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("BILL RECEIPT"),
                    new MessageFormat("Page - {0}"));
            if (!printed) {
                JOptionPane.showMessageDialog(frame, "Printing canceled", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to print bill!\n" + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}