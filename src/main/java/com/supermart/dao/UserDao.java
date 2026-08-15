package com.supermart.dao;

import com.supermart.config.Database;
import com.supermart.model.Role;
import com.supermart.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDao {

    private final Database database;

    public UserDao(Database database) {
        this.database = database;
    }

    /**
     * Look a login up by username.
     *
     * <p>SECURITY: the username is bound as a parameter, never concatenated into the SQL text. A
     * payload such as {@code ' OR '1'='1} is therefore treated as a literal username that simply
     * does not exist, and no row comes back. See {@code SqlInjectionTest}.
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, full_name, password_hash, role FROM users WHERE username = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load user " + username, e);
        }
    }

    public User create(String username, String fullName, String passwordHash, Role role) {
        String sql = "INSERT INTO users (username, full_name, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, passwordHash);
            ps.setString(4, role.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : 0;
                return new User(id, username, fullName, passwordHash, role);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user " + username, e);
        }
    }

    public int count() {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count users", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("password_hash"),
                Role.of(rs.getString("role")));
    }
}
