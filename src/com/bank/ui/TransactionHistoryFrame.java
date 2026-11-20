package com.bank.ui;

import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.service.BankService;
import com.bank.ui.UITheme.*;
import com.bank.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class TransactionHistoryFrame extends JFrame {
    private final Customer customer;
    private final BankService bankService;

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel countBadge;
    private ModernButton exportBtn;
    private ModernButton refreshBtn;

    public TransactionHistoryFrame(Customer customer, BankService bankService) {
        this.customer = customer;
        this.bankService = bankService;
        initUI();
        loadTransactions();
    }

    private void initUI() {
        setTitle("Passbook & Transaction History — " + customer.getName());
        setSize(820, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        mainPanel.setBackground(UITheme.BG_MAIN);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("📜 Account Transaction Statement");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_PRIMARY);

        countBadge = new JLabel("Loading...  ");
        countBadge.setFont(UITheme.FONT_BOLD);
        countBadge.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(countBadge, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Activity Type", "Amount (₹)", "Description", "Timestamp"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        sorter = new TableRowSorter<>(model);
        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 2));
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(UITheme.TEXT_DARK);

        // Header style
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.getTableHeader().setBackground(UITheme.COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);

        // Column Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);

        table.getColumnModel().getColumn(1).setCellRenderer(new TransactionTypeRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(120);

        table.getColumnModel().getColumn(2).setCellRenderer(new AmountRenderer());
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        exportBtn = new ModernButton("📄 Export Statement", UITheme.COLOR_SECONDARY, UITheme.COLOR_SECONDARY_HOVER);
        exportBtn.setPreferredSize(new Dimension(150, 34));
        exportBtn.addActionListener(e -> exportStatement());

        refreshBtn = new ModernButton("🔃 Refresh", UITheme.COLOR_PRIMARY, UITheme.COLOR_PRIMARY_HOVER);
        refreshBtn.setPreferredSize(new Dimension(110, 34));
        refreshBtn.addActionListener(e -> loadTransactions());

        ModernButton closeBtn = new ModernButton("Close", UITheme.COLOR_DANGER, UITheme.COLOR_DANGER_HOVER);
        closeBtn.setPreferredSize(new Dimension(100, 34));
        closeBtn.addActionListener(e -> dispose());

        footer.add(exportBtn);
        footer.add(refreshBtn);
        footer.add(closeBtn);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadTransactions() {
        refreshBtn.setEnabled(false);
        refreshBtn.setText("Loading...");

        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Transaction> doInBackground() {
                return bankService.getTransactionHistory(customer.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Transaction> transactions = get();
                    model.setRowCount(0);
                    for (Transaction t : transactions) {
                        model.addRow(new Object[]{
                            String.format("#%04d", t.getId()),
                            t.getType(),
                            t.getAmount(), // Store raw amount for sorting
                            t.getDescription(),
                            t.getTimestamp()
                        });
                    }
                    updateCountBadge(transactions.size());
                } catch (Exception ex) {
                    showError("Failed to load transactions: " + ex.getMessage());
                } finally {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("🔃 Refresh");
                }
            }
        };
        worker.execute();
    }

    private void updateCountBadge(int count) {
        countBadge.setText("Total Transactions: " + count + "  ");
    }

    private void exportStatement() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("statement_" + customer.getName().replace(" ", "_") + ".txt"));
        int result = fc.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                List<Transaction> transactions = bankService.getTransactionHistory(customer.getId());
                bankService.exportStatement(customer.getId(), customer.getName(), transactions,
                    fc.getSelectedFile().getAbsolutePath());
                showSuccess("Statement exported successfully to:\n" + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                showError("Failed to export statement: " + ex.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Custom renderer for transaction types with color coding
    private static class TransactionTypeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(UITheme.FONT_BOLD);
            String type = val != null ? val.toString() : "";
            if (!isSel) {
                if (type.contains("DEPOSIT") || type.contains("TRANSFER_IN")) {
                    setForeground(new Color(5, 150, 105)); // Green
                } else {
                    setForeground(new Color(220, 38, 38)); // Red
                }
            }
            return comp;
        }
    }

    // Custom renderer for amounts with right alignment and sign
    private static class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setFont(new Font("Segoe UI", Font.BOLD, 13));

            if (val instanceof Number) {
                double amount = ((Number) val).doubleValue();
                String type = "";
                Object typeObj = tbl.getModel().getValueAt(r, 1);
                if (typeObj != null) type = typeObj.toString();

                if (type.contains("DEPOSIT") || type.contains("TRANSFER_IN")) {
                    setText("+ ₹" + String.format("%,.2f", amount));
                    if (!isSel) setForeground(new Color(5, 150, 105));
                } else {
                    setText("- ₹" + String.format("%,.2f", amount));
                    if (!isSel) setForeground(new Color(220, 38, 38));
                }
            } else if (val != null) {
                setText(val.toString());
            }
            return comp;
        }
    }
}