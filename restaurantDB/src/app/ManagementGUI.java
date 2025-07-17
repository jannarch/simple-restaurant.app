
package app;

import javax.swing.*;   
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;


public class ManagementGUI extends JFrame {  // Make sure it extends JFrame
    private static final long serialVersionUID = 1L;
    private final DatabaseManager databaseManager;
    private final String[] tableNames = {"customers", "employees", "menu"};
    private final JComboBox<String> tableSelector = new JComboBox<>(tableNames);
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel inputPanel;  // Panel to hold dynamic input fields
    private JComponent[] inputFields;
    private JTextField nameField;  // Input field for customer name
    private JTextField orderField; // Input field for order menu

    public ManagementGUI(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
        setTitle("Restaurant Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Centers the window on the screen
    }

   

    public void initializeGUI() {
        // Top Panel for Table Selector
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Table:"));
        topPanel.add(tableSelector);
        

        // Center Panel for Table Display
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Bottom Panel for CRUD Operations
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(2, 1));

        inputPanel = new JPanel();  // Initialize input panel here
        bottomPanel.add(inputPanel);  // Add to bottomPanel

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnGenerateReport = new JButton("Generate Report");
        JButton btnGenerateBill = new JButton("Generate Bill");
        
        buttonPanel.add(btnGenerateBill);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnGenerateReport);

        btnGenerateReport.addActionListener(e -> generateReport());
        bottomPanel.add(buttonPanel);

        // Adding panels to the frame
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
     // Initialize specific input fields for name and order
        initializeInputFields();
        
        // Event Handling
        tableSelector.addActionListener(e -> loadTableData());
        btnAdd.addActionListener(e -> addRecord());
        btnUpdate.addActionListener(e -> updateRecord());
        btnDelete.addActionListener(e -> deleteRecord());
        btnGenerateBill.addActionListener(e -> generateBill());
        
     // Table selection listener for populating input fields
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int selectedRow = table.getSelectedRow();
                String customerName = (String) table.getValueAt(selectedRow, 1); // Column 1: customer_name
                String orderMenu = (String) table.getValueAt(selectedRow, 2);   // Column 2: order_menu

                // Populate input fields
                nameField.setText(customerName);
                orderField.setText(orderMenu);
                

            }
        });
        
        // Initial Data Load
        loadTableData();
    }
    
    private void initializeInputFields() {
        nameField = new JTextField(20);
        orderField = new JTextField(20);

        inputPanel.setLayout(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Nama Pelanggan:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Kode Pesanan:"));
        inputPanel.add(orderField);
    }

    private void loadTableData() {
        try {
            String selectedTable = (String) tableSelector.getSelectedItem();
            if (selectedTable == null) return;

            String[] columns = getTableColumns(selectedTable);
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            List<String[]> records = databaseManager.getAllRecords(selectedTable, columns);
            for (String[] record : records) {
                tableModel.addRow(record);
            }

            // Update Input Fields based on table columns
            updateInputFields(columns);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading data: " + ex.getMessage());
        }
    }

    private void updateInputFields(String[] columns) {
        // Clear previous input fields
        inputPanel.removeAll();

        // Create new input fields based on the number of columns
        int numColumns = columns.length;

        // If the table is 'customers', remove 'customer_id' from the list of columns
        if (columns[0].equals("customer_id")) {
            // Exclude 'customer_id' from the columns
            columns = Arrays.copyOfRange(columns, 1, columns.length); // Skip the first column (customer_id)
            numColumns--; // Adjust the number of columns
        }

        inputFields = new JComponent[numColumns]; // Make sure to include the adjusted number of columns
        inputPanel.setLayout(new GridLayout(1, numColumns)); // Update layout to match the number of fields

        for (int i = 0; i < numColumns; i++) { // Create input fields for each column
            if (columns[i].equals("status")) {
                JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Present", "Late", "Absent"});
                inputPanel.add(statusComboBox);
                inputFields[i] = statusComboBox;
            } else {
                JTextField inputField = new JTextField(10);
                inputPanel.add(inputField);
                inputFields[i] = inputField;
            }
        }

        // Refresh the panel to reflect changes
        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void generateBill() {
        try {
            // Ambil data pelanggan yang dipilih
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih pelanggan untuk menghasilkan struk.");
                return;
            }

            // Ambil informasi dari tabel 'customers'
            String customerName = (String) tableModel.getValueAt(selectedRow, 1); // customer_name
            String orderMenuCode = (String) tableModel.getValueAt(selectedRow, 2); // order_menu (kode pesanan)

            // Ambil informasi dari tabel 'menu' berdasarkan menu_item_id
            String[] menuColumns = getTableColumns("menu");
            List<String[]> menuRecords = databaseManager.getAllRecords("menu", menuColumns);

            String orderMenuName = null;
            double totalPrice = 0;
            boolean itemFound = false;

            for (String[] menuRecord : menuRecords) {
                if (menuRecord[0].equals(orderMenuCode)) { // Cocokkan menu_item_id (kolom pertama)
                    orderMenuName = menuRecord[1]; // Ambil item_name (kolom kedua)
                    totalPrice = Double.parseDouble(menuRecord[2]); // Ambil price (kolom ketiga)
                    itemFound = true;
                    break;
                }
            }

            if (!itemFound) {
                JOptionPane.showMessageDialog(this, "Kode pesanan '" + orderMenuCode + "' tidak ditemukan di tabel menu.");
                return;
            }

            // Format struk
            String bill = "=== STRUK PEMBAYARAN ===\n" +
                          "Nama Pelanggan: " + customerName + "\n" +
                          "Pesanan: " + orderMenuName + "\n" +
                          "Total Harga: Rp " + String.format("%.2f", totalPrice) + "\n" +
                          "=========================";

            // Tampilkan struk di dialog
            JOptionPane.showMessageDialog(this, bill, "Struk Pembayaran", JOptionPane.INFORMATION_MESSAGE);

            // Simpan struk ke file
            String fileName = customerName.replace(" ", "_") + "_bill.txt";
            java.nio.file.Files.write(java.nio.file.Paths.get(fileName), bill.getBytes());
            JOptionPane.showMessageDialog(this, "Struk disimpan ke file: " + fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghasilkan struk: " + ex.getMessage());
        }
    }

    private String[] getTableColumns(String tableName) {
        return switch (tableName) {
            case "customers" -> new String[]{"customer_id", "customer_name", "order_menu"};
            case "employees" -> new String[]{"employee_id", "date", "status"};
            case "menu" -> new String[]{"menu_item_id", "item_name", "price"};
            default -> new String[]{};
        };
    }

    private void addRecord() {
        try {
            String selectedTable = (String) tableSelector.getSelectedItem();
            if (selectedTable == null) return;

            String[] columns = getTableColumns(selectedTable);
            
            // If the table is 'customers', exclude 'customer_id'
            if (selectedTable.equals("customers")) {
                // Create a new array without the 'customer_id' column
                columns = removeCustomerIdColumn(columns);
            }

            String[] values = new String[columns.length]; // Include all columns (except 'customer_id')

            for (int i = 0; i < columns.length; i++) {
                if (inputFields[i] instanceof JTextField) {
                    values[i] = ((JTextField) inputFields[i]).getText();
                } else if (inputFields[i] instanceof JComboBox) {
                    values[i] = (String) ((JComboBox<?>) inputFields[i]).getSelectedItem();
                }
            }

            // Now insert the record with the updated columns and values
            if (databaseManager.insertRecord(selectedTable, columns, values)) {
                loadTableData();
                JOptionPane.showMessageDialog(null, "Record added successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add record.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding record: " + ex.getMessage());
        }
    }

    // Helper method to remove 'customer_id' column for 'customers' table
    private String[] removeCustomerIdColumn(String[] columns) {
        int length = columns.length;
        String[] newColumns = new String[length - 1];
        int index = 0;

        for (int i = 0; i < length; i++) {
            if (!columns[i].equals("customer_id")) {
                newColumns[index++] = columns[i];
            }
        }

        return newColumns;
    }

    private void updateRecord() {
        try {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a row to update.");
                return;
            }

            String selectedTable = (String) tableSelector.getSelectedItem();
            if (selectedTable == null) return;

            String[] columns = getTableColumns(selectedTable);
            String id = (String) tableModel.getValueAt(selectedRow, 0); // Primary key value

            // Exclude the ID column for update
            String[] updateColumns = Arrays.copyOfRange(columns, 1, columns.length);
            String[] values = new String[updateColumns.length];

            // Collect updated values from input fields
            for (int i = 0; i < updateColumns.length; i++) {
                if (inputFields[i] instanceof JTextField) {
                    values[i] = ((JTextField) inputFields[i]).getText();
                } else if (inputFields[i] instanceof JComboBox) {
                    values[i] = (String) ((JComboBox<?>) inputFields[i]).getSelectedItem();
                }
                // Check for empty fields
                if (values[i] == null || values[i].isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill out all fields.");
                    return;
                }
            }

            // Construct WHERE clause
            String whereClause = columns[0] + " = '" + id + "'"; // Wrap in quotes for string IDs

            // Perform the update
            if (databaseManager.updateRecord(selectedTable, updateColumns, values, whereClause)) {
                loadTableData(); // Refresh table
                JOptionPane.showMessageDialog(null, "Record updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to update record.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating record: " + ex.getMessage());
        }
    }


    private void deleteRecord() {
        try {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a row to delete.");
                return;
            }

            String selectedTable = (String) tableSelector.getSelectedItem();
            if (selectedTable == null) return;

            String[] columns = getTableColumns(selectedTable);
            String id = (String) tableModel.getValueAt(selectedRow, 0);
            String whereClause = columns[0] + " = " + id; // ID column

            if (databaseManager.deleteRecord(selectedTable, whereClause)) {
                loadTableData();
                JOptionPane.showMessageDialog(null, "Record deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete record.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting record: " + ex.getMessage());
        }
    }
    
    private void generateReport() {
        try {
            String selectedTable = (String) tableSelector.getSelectedItem();
            if (selectedTable == null) {
                JOptionPane.showMessageDialog(this, "Please select a table to generate a report.");
                return;
            }

            String[] columns = getTableColumns(selectedTable);
            List<String[]> records = databaseManager.getAllRecords(selectedTable, columns);

            StringBuilder report = new StringBuilder();
            // Add column headers
            for (String column : columns) {
                report.append(column).append(",");
            }
            report.setLength(report.length() - 1); // Remove the trailing comma
            report.append("\n");

            // Add records
            for (String[] record : records) {
                for (String value : record) {
                    report.append(value).append(",");
                }
                report.setLength(report.length() - 1); // Remove the trailing comma
                report.append("\n");
            }

            // Save to a file (e.g., CSV)
            String fileName = selectedTable + "_report.csv";
            java.nio.file.Files.write(java.nio.file.Paths.get(fileName), report.toString().getBytes());
            JOptionPane.showMessageDialog(this, "Report generated successfully: " + fileName);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
        }
    }
}