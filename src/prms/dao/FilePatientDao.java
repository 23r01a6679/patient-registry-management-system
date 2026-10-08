package prms.dao;

import prms.model.Patient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Persists patients to a tab-separated text file so data survives restarts. */
public class FilePatientDao implements PatientDao {
    private final Path file;
    private final List<Patient> patients = new ArrayList<>();

    public FilePatientDao(Path file) {
        this.file = file;
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            if (Files.exists(file)) {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    if (!line.isBlank()) patients.add(Patient.fromRecord(line));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    @Override public List<Patient> findAll() { return new ArrayList<>(patients); }

    @Override public int nextId() {
        return patients.stream().mapToInt(Patient::getId).max().orElse(0) + 1;
    }

    @Override public void add(Patient patient) { patients.add(patient); save(); }

    @Override public void update(Patient patient) {
        // Patient objects are shared references, so edits are already in the list; persist them.
        save();
    }

    @Override public boolean delete(int id) {
        boolean removed = patients.removeIf(p -> p.getId() == id);
        if (removed) save();
        return removed;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Patient p : patients) lines.add(p.toRecord());
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }
}
