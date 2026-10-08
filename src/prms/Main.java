package prms;

import prms.dao.FilePatientDao;
import prms.service.AuthService;
import prms.service.PatientService;
import prms.ui.LoginFrame;

import javax.swing.*;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            PatientService service = new PatientService(new FilePatientDao(Paths.get("data", "patients.csv")));
            new LoginFrame(new AuthService(), service).setVisible(true);
        });
    }
}
