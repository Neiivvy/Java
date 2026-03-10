package BillingSystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/billing_system";
    private static final String USER     = "root";
    private static final String PASSWORD = "";  // XAMPP default has no password

    private static Connection connection = null;

    // Returns a single shared connection (creates one if not yet open)
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully.");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found!");
                throw new SQLException("Driver not found: " + e.getMessage());
            }
        }
        return connection;
    }

    // Call this when the app closes
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}