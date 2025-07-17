package app;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class MainApp {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            DatabaseManager dbManager = new DatabaseManager(connection);
            ManagementGUI gui = new ManagementGUI(dbManager);
            gui.initializeGUI();  // Call your method to initialize the GUI (set up tables, etc.)
            gui.setVisible(true);  // Now you can set it visible

            // Add a shutdown hook to close the connection on exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    DatabaseConnection.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to start application: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
