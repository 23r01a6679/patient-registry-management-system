package prms.service;

import prms.dao.PatientDao;
import prms.model.Patient;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Business logic: validation, search, statistics. No Swing code here, so it is easy to unit test. */
public class PatientService {
    private final PatientDao dao;

    public PatientService(PatientDao dao) { this.dao = dao; }

    public Patient register(String name, String ageText, String gender, String issue) {
        Patient p = new Patient(dao.nextId(), name, parseAge(ageText), gender, issue, LocalDate.now());
        dao.add(p);
        return p;
    }

    public void update(int id, String name, String ageText, String gender, String issue) {
        Patient p = find(id);
        p.setName(name);
        p.setAge(parseAge(ageText));
        p.setGender(gender);
        p.setHealthIssue(issue);
        dao.update(p);
    }

    public void delete(int id) {
        if (!dao.delete(id)) throw new IllegalArgumentException("Patient " + id + " not found");
    }

    /** Case-insensitive match on id, name, gender or health issue. Blank query returns everyone. */
    public List<Patient> search(String query) {
        if (query == null || query.isBlank()) return dao.findAll();
        String q = query.trim().toLowerCase(Locale.ROOT);
        return dao.findAll().stream()
                .filter(p -> String.valueOf(p.getId()).equals(q)
                        || p.getName().toLowerCase(Locale.ROOT).contains(q)
                        || p.getGender().toLowerCase(Locale.ROOT).contains(q)
                        || p.getHealthIssue().toLowerCase(Locale.ROOT).contains(q))
                .collect(Collectors.toList());
    }

    public String summary() {
        List<Patient> all = dao.findAll();
        double avg = all.stream().mapToInt(Patient::getAge).average().orElse(0);
        long m = all.stream().filter(p -> p.getGender().equals("Male")).count();
        long f = all.stream().filter(p -> p.getGender().equals("Female")).count();
        long o = all.size() - m - f;
        return String.format("Total: %d  |  Average age: %.1f  |  Male: %d  Female: %d  Other: %d",
                all.size(), avg, m, f, o);
    }

    private Patient find(int id) {
        return dao.findAll().stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Patient " + id + " not found"));
    }

    private static int parseAge(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Age must be a whole number");
        }
    }
}
