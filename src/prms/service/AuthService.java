package prms.service;

import prms.model.Role;
import prms.model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/** Login with salted PBKDF2 password hashes. Plain-text passwords are never stored. */
public class AuthService {
    private static final int ITERATIONS = 65_536;

    private static class Credential {
        final byte[] salt;
        final byte[] hash;
        final Role role;
        Credential(byte[] salt, byte[] hash, Role role) { this.salt = salt; this.hash = hash; this.role = role; }
    }

    private final Map<String, Credential> users = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    /** Demo accounts. In a real system these would live in a database table. */
    public AuthService() {
        addUser("admin", "admin123", Role.ADMIN);
        addUser("doctor", "doctor123", Role.DOCTOR);
    }

    public void addUser(String username, String password, Role role) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        users.put(username, new Credential(salt, hash(password.toCharArray(), salt), role));
    }

    /** Returns the logged-in user, or null if credentials are wrong. */
    public User login(String username, String password) {
        Credential c = users.get(username == null ? "" : username.trim());
        if (c == null || password == null) return null;
        boolean ok = MessageDigest.isEqual(c.hash, hash(password.toCharArray(), c.salt));
        return ok ? new User(username.trim(), c.role) : null;
    }

    private static byte[] hash(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        }
    }
}
