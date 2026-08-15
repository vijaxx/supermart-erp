package com.supermart.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thin JDBC connection provider.
 *
 * <p>The project targets plain JDBC deliberately - every DAO obtains a {@link Connection} here,
 * uses {@code PreparedStatement}, and closes it with try-with-resources. Swapping H2 for MySQL is a
 * matter of changing the URL and driver dependency; no DAO code would change.
 */
public class Database {

    private final String url;
    private final String username;
    private final String password;

    public Database(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /** File-backed database used by the running application (survives restarts). */
    public static Database file(String path) {
        return new Database("jdbc:h2:" + path + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    }

    /** In-memory database used by the test-suite - one isolated schema per name. */
    public static Database inMemory(String name) {
        return new Database("jdbc:h2:mem:" + name + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public String getUrl() {
        return url;
    }
}
