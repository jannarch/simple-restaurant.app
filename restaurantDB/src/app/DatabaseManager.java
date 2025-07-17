package app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    // Fetch all records from a table
    public List<String[]> getAllRecords(String tableName, String[] columns) throws SQLException {
        List<String[]> records = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String[] row = new String[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    row[i] = rs.getString(columns[i]);
                }
                records.add(row);
            }
        }
        return records;
    }

    // Insert a new record into a table
    public boolean insertRecord(String tableName, String[] columns, String[] values) {
        // Validate that the number of columns matches the number of values
        if (columns.length != values.length) {
            System.err.println("Error: Number of columns and values do not match.");
            return false;
        }

        // Construct the SQL insert statement dynamically
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]);
            if (i < columns.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(") VALUES (");
        for (int i = 0; i < columns.length; i++) {
            sql.append("?");
            if (i < columns.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            // Set the values dynamically based on the number of columns
            for (int i = 0; i < values.length; i++) {
                stmt.setString(i + 1, values[i]); // Parameter indexing starts from 1
            }

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0; // Return true if the record was added successfully
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update a record in a table
    public boolean updateRecord(String tableName, String[] columns, String[] values, String whereClause) throws SQLException {
        // Debugging output
        System.out.println("Columns array length: " + columns.length);
        System.out.println("Values array length: " + values.length);

        // Ensure columns and values arrays have the same length
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Columns and values array must have the same length.");
        }

        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        
        // Append columns and placeholders to the SQL query
        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]).append(" = ?, ");
        }
        
        // Remove the trailing comma and space, then append WHERE clause
        sql.delete(sql.length() - 2, sql.length()).append(" WHERE ").append(whereClause);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            // Set the values for the placeholders in the PreparedStatement
            for (int i = 0; i < values.length; i++) {
                stmt.setString(i + 1, values[i]); // Set each value to the corresponding placeholder
            }

            // Execute the update and return true if at least one row was updated
            return stmt.executeUpdate() > 0;
        }
    }


    // Delete a record from a table
    public boolean deleteRecord(String tableName, String whereClause) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql) > 0;
        }
    }
}
