package api.server.controller;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import api.server.dao.CryptoService;
import api.server.dao.UserRepository;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UserRepository repo;
    private final CryptoService crypto;

    public LoginController(UserRepository repo, CryptoService crypto) {
        this.repo = repo;
        this.crypto = crypto;
    }

    // Health for load balancers / sanity checks
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    // Secure registration: parameterized INSERT + salted PBKDF2
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        // Layer 7 validation: reject sketchy usernames and weak passwords
        if (!username.matches("^[A-Za-z0-9_]{3,32}$") || password.length() < 8) {
            return ResponseEntity.badRequest().body("Invalid input");
        }

        try {
            // Generate per-user salt
            String saltB64 = crypto.newSaltB64();

            // Derive PBKDF2 hash (no plaintext password stored)
            String hashB64 = crypto.hashPasswordB64(password, saltB64);

            // Insert using a prepared statement in UserRepository (prevents SQL injection)
            boolean ok = repo.createUser(username, hashB64, saltB64);
            if (!ok) {
                return ResponseEntity.status(409).body("User exists");
            }

            String safeUser = StringEscapeUtils.escapeHtml4(username);
            return ResponseEntity.ok("User " + safeUser + " created");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error");
        }
    }

    // Secure login: parameterized SELECT + PBKDF2 verify
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        // Block clearly malicious usernames before even touching the DB
        if (!username.matches("^[A-Za-z0-9_]{3,32}$")) {
            return ResponseEntity.badRequest().body("Invalid input");
        }

        return repo.findByUsername(username)
                .map(row -> {
                    try {
                        // Recompute PBKDF2(suppliedPassword, storedSalt)
                        String attemptHash = crypto.hashPasswordB64(password, row.passwordSaltB64());

                        // Constant-time compare to avoid timing side channels
                        if (crypto.slowEquals(row.passwordHashB64(), attemptHash)) {
                            String safeUser = StringEscapeUtils.escapeHtml4(username);
                            return ResponseEntity.ok("Welcome, " + safeUser + "!");
                        } else {
                            return ResponseEntity.status(401).body("Invalid credentials");
                        }
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Server error");
                    }
                })
                .orElseGet(() ->
                        ResponseEntity.status(401).body("Invalid credentials")
                );
    }
}
