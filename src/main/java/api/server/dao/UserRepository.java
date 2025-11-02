package api.server.dao;

import api.server.models.UserRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<UserRow> USER_MAPPER = (rs, i) -> new UserRow(
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("password_salt")
    );

    public Optional<UserRow> findByUsername(String username) {
        var sql = "SELECT username, password_hash, password_salt FROM users WHERE username = ?";
        return jdbc.query(sql, USER_MAPPER, username).stream().findFirst();
    }

    public boolean createUser(String username, String hashB64, String saltB64) {
        var sql = "INSERT INTO users (username, password_hash, password_salt) VALUES (?, ?, ?)";
        return jdbc.update(sql, username, hashB64, saltB64) == 1;
    }
}

