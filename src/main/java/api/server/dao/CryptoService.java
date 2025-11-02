package api.server.dao;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class CryptoService {
    private static final int ITER = 65536;
    private static final int KEY_BITS = 256;

    public String newSaltB64() {
        byte[] s = new byte[16];
        new SecureRandom().nextBytes(s);
        return Base64.getEncoder().encodeToString(s);
    }

    public String hashPasswordB64(String password, String saltB64) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltB64);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER, KEY_BITS);
        var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return Base64.getEncoder().encodeToString(skf.generateSecret(spec).getEncoded());
    }

    public boolean slowEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }
}

