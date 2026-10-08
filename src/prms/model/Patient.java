package prms.model;

import java.time.LocalDate;

/** Domain object. Validates itself, so invalid state can never exist. */
public class Patient {
    private final int id;
    private String name;
    private int age;
    private String gender;
    private String healthIssue;
    private final LocalDate registeredOn;

    public Patient(int id, String name, int age, String gender, String healthIssue, LocalDate registeredOn) {
        this.id = id;
        this.registeredOn = registeredOn;
        setName(name);
        setAge(age);
        setGender(gender);
        setHealthIssue(healthIssue);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getHealthIssue() { return healthIssue; }
    public LocalDate getRegisteredOn() { return registeredOn; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        this.name = clean(name);
    }

    public void setAge(int age) {
        if (age < 0 || age > 120) throw new IllegalArgumentException("Age must be between 0 and 120");
        this.age = age;
    }

    public void setGender(String gender) {
        if (gender == null || gender.isBlank()) throw new IllegalArgumentException("Gender is required");
        this.gender = clean(gender);
    }

    public void setHealthIssue(String healthIssue) {
        if (healthIssue == null || healthIssue.isBlank()) throw new IllegalArgumentException("Health issue is required");
        this.healthIssue = clean(healthIssue);
    }

    /** Tabs/newlines would corrupt the tab-separated storage file. */
    private static String clean(String s) {
        return s.trim().replaceAll("[\\t\\r\\n]+", " ");
    }

    public String toRecord() {
        return id + "\t" + name + "\t" + age + "\t" + gender + "\t" + healthIssue + "\t" + registeredOn;
    }

    public static Patient fromRecord(String line) {
        String[] p = line.split("\t", -1);
        if (p.length != 6) throw new IllegalArgumentException("Corrupt record: " + line);
        return new Patient(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]), p[3], p[4], LocalDate.parse(p[5]));
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Age: " + age + ", Gender: " + gender + ", Health Issue: " + healthIssue;
    }
}
