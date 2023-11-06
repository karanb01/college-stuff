import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class GUI {
    private DefaultTableModel tableModel;
    private JTextField expenseNameField;
    private JTextField expenseAmountField;
    private JButton clearDataButton;
    private JTable existingEntriesTable;

    public GUI() {
        JFrame frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new BorderLayout());

        expenseNameField = new JTextField();
        expenseAmountField = new JTextField();

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Expense Name");
        tableModel.addColumn("Amount");
        JTable expenseTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(expenseTable);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Expense Name:"));
        inputPanel.add(expenseNameField);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(expenseAmountField);


        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Expense");
        buttonPanel.add(addButton);

        clearDataButton = new JButton("Clear All Data");
        buttonPanel.add(clearDataButton); 

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);


        existingEntriesTable = expenseTable;

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addExpense();
            }
        });

        clearDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllData(existingEntriesTable);
            }
        });


        displayExpenses();
        frame.setVisible(true);
    }

    private void addExpense() {
        String name = expenseNameField.getText();
        String amount = expenseAmountField.getText();
        tableModel.addRow(new Object[]{name, amount});

        // database connection
        String insertSQL = "INSERT INTO expenses (name, amount) VALUES (?, ?)";
        
        try (Connection connection = DatabaseConnection.connect(); PreparedStatement ps = connection.prepareStatement(insertSQL)) {

            ps.setString(1, name);
            ps.setDouble(2, Double.parseDouble(amount));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        expenseNameField.setText("");
        expenseAmountField.setText("");
    }

    private void displayExpenses() {
        List<Expense> expenses = DatabaseConnection.getExpenses();
        for (Expense expense : expenses) {
            String name = expense.getName();
            double amount = expense.getAmount();
            tableModel.addRow(new Object[]{name, amount});
        }
    }

    private void clearAllData(JTable table) {
        try {
            Connection connection = DatabaseConnection.connect();

            String deleteSQL = "DELETE FROM expenses";

 
            PreparedStatement ps = connection.prepareStatement(deleteSQL);
            ps.executeUpdate();

            // Close resources
            ps.close();
            connection.close();

            // Clear the JTable displaying existing entries
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            System.out.println("All data cleared from the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI());
    }
}
