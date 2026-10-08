# Patient Registry Management System

A Java desktop application (Swing) for registering and managing patient records, with role-based login,
search, edit/delete and persistent storage. Built with a layered architecture (model / DAO / service / UI).

## Features

* Login with salted PBKDF2 password hashing; roles: ADMIN (add, edit, delete) and DOCTOR (add, edit)
* Add, update and delete patient records with input validation (no crashes on bad input)
* Live search by name, health issue, gender or ID
* Summary statistics (total, average age, gender split)
* Data saved to `data/patients.csv` and reloaded on startup
* Automated tests for validation, search, persistence and authentication

## Architecture

```
prms/model    Patient, User, Role         (domain objects, self-validating)
prms/dao      PatientDao (interface), FilePatientDao   (storage; swappable for JDBC)
prms/service  PatientService, AuthService  (business logic, no Swing code)
prms/ui       LoginFrame, MainFrame        (Swing views)
```

## Run (JDK 17+)

```
mkdir out
javac -d out $(find src -name "\*.java")
java -cp out prms.Main
```

Demo logins: `admin / admin123`, `doctor / doctor123`

## Test

```
javac -d out $(find src test -name "\*.java")
java -cp out prms.AppTest
```

## Possible next steps

Replace `FilePatientDao` with a JDBC/MySQL implementation, move users to a database table, add appointment scheduling.

**First description**

\## Screenshots

!\[Login](screenshots/login.png)

!\[Main screen](screenshots/main.png)

!\[Doctor role: Delete disabled](screenshots/doctor-no-delete.png)

