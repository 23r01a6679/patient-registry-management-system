package prms.dao;

import prms.model.Patient;
import java.util.List;

/** Storage contract. The service depends on this interface, so file storage can be swapped for JDBC/MySQL later. */
public interface PatientDao {
    List<Patient> findAll();
    int nextId();
    void add(Patient patient);
    void update(Patient patient);
    boolean delete(int id);
}
