package prms.ui;

import prms.model.Patient;
import prms.model.User;
import prms.service.PatientService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MainFrame extends JFrame {
    private final PatientService service;
    private final User user;

    private final JTextField nameField = new JTextField(18);
    private final JTextField ageField = new JTextField(18);
    private final JTextField issueField = new JTextField(18);
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JTextField searchField = new JTextField(20);
    private final JLabel statusLabel = new JLabel(" ");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Age", "Gender", "Health Issue", "Registered"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public MainFrame(User user, PatientService service) {
        this.user = user;
        this.service = service;
        setTitle("Patient Registry Management System  -  " + user.getUsername() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Patient details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 3; panel.add(ageField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1; panel.add(genderBox, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Health Issue:"), gbc);
        gbc.gridx = 3; panel.add(issueField, gbc);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> run(() -> {
            service.register(nameField.getText(), ageField.getText(), selectedGender(), issueField.getText());
            clearForm();
        }));
        updateBtn.addActionListener(e -> run(() -> {
            service.update(requireSelectedId(), nameField.getText(), ageField.getText(),
                    selectedGender(), issueField.getText());
            clearForm();
        }));
        deleteBtn.addActionListener(e -> {
            int id;
            try { id = requireSelectedId(); } catch (IllegalStateException ex) { showError(ex.getMessage()); return; }
            int ok = JOptionPane.showConfirmDialog(this, "Delete patient " + id + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) run(() -> { service.delete(id); clearForm(); });
        });
        clearBtn.addActionListener(e -> clearForm());

        // Role-based access control: buttons are disabled when the role lacks the permission.
        addBtn.setEnabled(user.getRole().canEdit());
        updateBtn.setEnabled(user.getRole().canEdit());
        deleteBtn.setEnabled(user.getRole().canDelete());
        deleteBtn.setToolTipText(user.getRole().canDelete() ? null : "Only admins can delete records");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(addBtn); buttons.add(updateBtn); buttons.add(deleteBtn); buttons.add(clearBtn);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttons, gbc);
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search (name, issue, gender, id):"));
        searchBar.add(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillFormFromSelection();
        });

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (Patient p : service.search(searchField.getText())) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(),
                    p.getHealthIssue(), p.getRegisteredOn()});
        }
        statusLabel.setText(service.summary());
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        ageField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        genderBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
        issueField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
    }

    private int requireSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) throw new IllegalStateException("Select a patient in the table first");
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private String selectedGender() { return (String) genderBox.getSelectedItem(); }

    private void clearForm() {
        nameField.setText(""); ageField.setText(""); issueField.setText("");
        genderBox.setSelectedIndex(0);
        table.clearSelection();
    }

    /** Runs an action, shows validation errors in a dialog instead of crashing, then refreshes the table. */
    private void run(Runnable action) {
        try {
            action.run();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.WARNING_MESSAGE);
    }
}
