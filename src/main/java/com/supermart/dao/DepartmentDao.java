package com.supermart.dao;

import com.supermart.config.Database;
import com.supermart.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDao {

    private final Database database;

    public DepartmentDao(Database database) {
        this.database = database;
    }

    public List<Department> findAll() {
        String sql = "SELECT id, name FROM departments ORDER BY name";
        List<Department> result = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Department(rs.getInt("id"), rs.getString("name")));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list departments", e);
        }
    }

    public Optional<Department> findById(int id) {
        String sql = "SELECT id, name FROM departments WHERE id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new Department(rs.getInt("id"), rs.getString("name")))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load department " + id, e);
        }
    }

    public Department create(String name) {
        String sql = "INSERT INTO departments (name) VALUES (?)";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return new Department(keys.next() ? keys.getInt(1) : 0, name);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create department " + name, e);
        }
    }
}
