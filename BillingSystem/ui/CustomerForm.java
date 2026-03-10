package BillingSystem.ui;

import javax.swing.*;
import java.awt.Font;
import BillingSystem.model.Customer;

public class CustomerForm {
    JFrame Frame;
    JLabel titleLabel, nameLabel, phoneLabel, addressLabel;
    JTextField nameField, phoneField, addressField;
    JButton nextButton, clearButton;

    public CustomerForm() {
        Frame = new JFrame("Customer Information");
        Frame.setSize(600, 500);
        Frame.setLayout(null);
        Frame.setLocationRelativeTo(null);

        // Title
        titleLabel = new JLabel("Fill Customer Details", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(150, 20, 300, 40);
        Frame.add(titleLabel);

        nameLabel = new JLabel("Name:");
        nameLabel.setBounds(100, 100, 100, 30);
        Frame.add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(220, 100, 220, 30);
        Frame.add(nameField);

        phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(100, 150, 100, 30);
        Frame.add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setBounds(220, 150, 220, 30);
        Frame.add(phoneField);

        addressLabel = new JLabel("Address:");
        addressLabel.setBounds(100, 200, 100, 30);
        Frame.add(addressLabel);

        addressField = new JTextField();
        addressField.setBounds(220, 200, 220, 30);
        Frame.add(addressField);

        nextButton = new JButton("Next");
        nextButton.setBounds(220, 300, 100, 35);
        Frame.add(nextButton);

        // Clear Button
        clearButton = new JButton("Clear");
        clearButton.setBounds(340, 300, 100, 35);
        Frame.add(clearButton);

        // Clear button action
        clearButton.addActionListener(e -> {
            nameField.setText("");
            phoneField.setText("");
            addressField.setText("");
            nameField.requestFocus();
        });

        nextButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

               if (name.isEmpty() && phone.isEmpty() && address.isEmpty()) {
        JOptionPane.showMessageDialog(Frame,
                "Please fill all the fields first!",
                "Empty Fields", JOptionPane.WARNING_MESSAGE);
        nameField.requestFocus();
        return;
    }

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
    // Constructor to pre-fill customer details (used when going back from ProductForm)
public CustomerForm(Customer customer) {
    this(); // call the default constructor to set up the UI

    // Fill fields with existing customer data
    nameField.setText(customer.getName());
    phoneField.setText(customer.getPhone());
    addressField.setText(customer.getAddress());
}
}