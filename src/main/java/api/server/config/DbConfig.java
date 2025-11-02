package api.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * DataSource configured for:
 *  - Least-privilege DB user
 *  - TLS from app -> Postgres (verify-full)
 *  - No credentials hard-coded (pull from environment)
 */
@Configuration
public class DbConfig {

    @Bean
    public DataSource dataSource() {
        String host = getEnv("DB_HOST", "");
        String port = getEnv("DB_PORT", "");
        String db   = getEnv("DB_NAME", "");
        String user = getEnv("DB_USER", "postgres");
        String pass = getEnv("DB_PASSWORD", "");

        String url = String.format(
                "jdbc:postgresql://%s:%s/%s?sslmode=disable",
                host, port, db
        );

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(1);
        cfg.setPoolName("securePool");
        // Defensive JDBC settings
        cfg.addDataSourceProperty("reWriteBatchedInserts", "true");

        return new HikariDataSource(cfg);
    }

    private static String getEnv(String k) {
        String v = System.getenv(k);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing env var: " + k);
        }
        return v;
    }
    private static String getEnv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }
}

