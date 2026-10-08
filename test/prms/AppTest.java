package prms;

import prms.dao.FilePatientDao;
import prms.model.Role;
import prms.model.User;
import prms.service.AuthService;
import prms.service.PatientService;

import java.nio.file.Files;
import java.nio.file.Path;

/** Dependency-free tests (run with: java -cp out prms.AppTest). Easy to port to JUnit 5. */
public class AppTest {
    static int passed = 0;

    static void check(boolean cond, String name) {
        if (!cond) throw new AssertionError("FAILED: " + name);
        passed++;
        System.out.println("PASS  " + name);
    }

    static boolean throwsIAE(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("prms", ".csv");
        Files.delete(file);
        PatientService svc = new PatientService(new FilePatientDao(file));

        svc.register("Ramu", "45", "Male", "cough");
        svc.register("Vinay", "23", "Male", "headache");
        svc.register("Ramya", "54", "Female", "cancer");
        check(svc.search("").size() == 3, "register three patients");
        check(svc.search("ram").size() == 2, "search by partial name (case-insensitive)");
        check(svc.search("HEADACHE").size() == 1, "search by health issue");
        check(svc.search("2").size() == 1, "search by id");

        check(throwsIAE(() -> svc.register("", "20", "Male", "flu")), "reject blank name");
        check(throwsIAE(() -> svc.register("A", "abc", "Male", "flu")), "reject non-numeric age");
        check(throwsIAE(() -> svc.register("A", "200", "Male", "flu")), "reject age out of range");
        check(throwsIAE(() -> svc.delete(99)), "reject deleting unknown id");

        svc.update(1, "Ramu K", "46", "Male", "fever");
        check(svc.search("fever").size() == 1, "update changes record");

        svc.delete(2);
        check(svc.search("").size() == 2, "delete removes record");

        PatientService reloaded = new PatientService(new FilePatientDao(file));
        check(reloaded.search("").size() == 2, "data persists across restarts");
        check(reloaded.search("Ramu K").size() == 1, "updated values persist");
        reloaded.register("New", "30", "Other", "flu");
        check(reloaded.search("New").get(0).getId() == 4, "ids keep increasing after delete (no reuse of max)");

        AuthService auth = new AuthService();
        User admin = auth.login("admin", "admin123");
        User doctor = auth.login("doctor", "doctor123");
        check(admin != null && admin.getRole() == Role.ADMIN, "admin login works");
        check(doctor != null && doctor.getRole() == Role.DOCTOR, "doctor login works");
        check(auth.login("admin", "wrong") == null, "wrong password rejected");
        check(auth.login("nobody", "x") == null, "unknown user rejected");
        check(Role.ADMIN.canDelete() && !Role.DOCTOR.canDelete(), "only admin may delete");

        Files.deleteIfExists(file);
        System.out.println("\nAll " + passed + " checks passed.");
    }
}
