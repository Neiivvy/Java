package BillingSystem.ui;

import javax.swing.*;
import BillingSystem.model.Customer;

public class CustomerForm {
    JFrame Frame;
    JLabel nameLabel, phoneLabel, addressLabel;
    JTextField nameField, phoneField, addressField;
    JButton nextButton;

    public CustomerForm() {
        Frame = new JFrame("Customer Information");
        Frame.setSize(600, 500);
        Frame.setLayout(null);

        nameLabel = new JLabel("Name:");
        nameLabel.setBounds(50, 50, 100, 30);
        Frame.add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(150, 50, 200, 30);
        Frame.add(nameField);

        phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(50, 100, 100, 30);
        Frame.add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setBounds(150, 100, 200, 30);
        Frame.add(phoneField);

        addressLabel = new JLabel("Address:");
        addressLabel.setBounds(50, 150, 100, 30);
        Frame.add(addressLabel);

        addressField = new JTextField();
        addressField.setBounds(150, 150, 200, 30);
        Frame.add(addressField);

        nextButton = new JButton("Next");
        nextButton.setBounds(200, 250, 100, 30);
        Frame.add(nextButton);

        nextButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            // --- Name: not empty, letters and spaces only ---
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(Frame,
                        "Please enter your Name!",
                        "Invalid Name", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }
            if (!name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(Frame,
                        "Please enter a valid Name!\n(Only letters and spaces allowed)",
                        "Invalid Name", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }

            // --- Phone: digits only, 7–15 digits ---
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(Frame,
                        "Please enter your Phone number!",
                        "Invalid Phone", JOptionPane.WARNING_MESSAGE);
                phoneField.requestFocus();
                return;
            }
            if (!phone.matches("\\d{7,15}")) {
                JOptionPane.showMessageDialog(Frame,
                        "Please enter a valid Phone number!\n(Digits only, 7 to 15 characters)",
                        "Invalid Phone", JOptionPane.WARNING_MESSAGE);
                phoneField.requestFocus();
                return;
            }

            // --- Address: not empty, at least 5 chars ---
            if (address.isEmpty()) {
                JOptionPane.showMessageDialog(Frame,
                        "Please enter your Address!",
                        "Invalid Address", JOptionPane.WARNING_MESSAGE);
                addressField.requestFocus();
                return;
            }
            if (address.length() < 5) {
                JOptionPane.showMessageDialog(Frame,
                        "Please enter a valid Address!\n(At least 5 characters required)",
                        "Invalid Address", JOptionPane.WARNING_MESSAGE);
                addressField.requestFocus();
                return;
            }

            // --- All valid: proceed ---
            Customer customer = new Customer(name, phone, address);
            new ProductForm(customer);
            Frame.dispose();
        });

        Frame.setVisible(true);
    }
}