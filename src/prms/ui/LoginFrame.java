package prms.ui;

import prms.model.User;
import prms.service.AuthService;
import prms.service.PatientService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    public LoginFrame(AuthService auth, PatientService service) {
        setTitle("Patient Registry - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; form.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; form.add(passField, gbc);

        JButton loginBtn = new JButton("Login");
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        form.add(loginBtn, gbc);

        Runnable doLogin = () -> {
            User user = auth.login(userField.getText(), new String(passField.getPassword()));
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login failed",
                        JOptionPane.ERROR_MESSAGE);
                passField.setText("");
                return;
            }
            new MainFrame(user, service).setVisible(true);
            dispose();
        };
        loginBtn.addActionListener(e -> doLogin.run());
        getRootPane().setDefaultButton(loginBtn);

        add(form);
        pack();
        setLocationRelativeTo(null);
    }
}
