package com.supermart.config;

import com.supermart.model.Role;
import com.supermart.security.PasswordHasher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the schema and loads demo data on first run so the application is usable straight
 * after {@code git clone} with no manual database setup.
 */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void initialize(Database database) {
        try (Connection connection = database.getConnection()) {
            runScript(connection, "schema.sql");
            if (isEmpty(connection, "departments")) {
                runScript(connection, "seed.sql");
            }
            if (isEmpty(connection, "users")) {
                seedUsers(connection);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize the database schema", e);
        }
    }

    private static boolean isEmpty(Connection connection, String table) throws SQLException {
        // Table name is a compile-time constant from this class only - never user input.
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    /**
     * Demo logins. These are throwaway credentials for a portfolio demo, documented in the README;
     * they are stored salted + PBKDF2-hashed exactly like a production password would be.
     */
    private static void seedUsers(Connection connection) throws SQLException {
        insertUser(connection, "admin", "Priya Nair (Store Manager)", "Admin@123", Role.ADMIN);
        insertUser(connection, "staff", "Ravi Kumar (Floor Staff)", "Staff@123", Role.STAFF);
    }

    private static void insertUser(Connection connection, String username, String fullName,
                                   String plainPassword, Role role) throws SQLException {
        String sql = "INSERT INTO users (username, full_name, password_hash, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, PasswordHasher.hash(plainPassword));
            ps.setString(4, role.name());
            ps.executeUpdate();
        }
    }

    private static void runScript(Connection connection, String resource) throws SQLException {
        for (String statementSql : readStatements(resource)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(statementSql);
            }
        }
    }

    private static List<String> readStatements(String resource) {
        try (InputStream in = SchemaInitializer.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing SQL resource: " + resource);
            }
            StringBuilder buffer = new StringBuilder();
            List<String> statements = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        continue;
                    }
                    buffer.append(line).append('\n');
                    if (trimmed.endsWith(";")) {
                        String sql = buffer.toString().trim();
                        statements.add(sql.substring(0, sql.length() - 1));
                        buffer.setLength(0);
                    }
                }
            }
            if (buffer.toString().trim().length() > 0) {
                statements.add(buffer.toString().trim());
            }
            return statements;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
