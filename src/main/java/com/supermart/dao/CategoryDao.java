package com.supermart.dao;

import com.supermart.config.Database;
import com.supermart.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    private final Database database;

    public CategoryDao(Database database) {
        this.database = database;
    }

    public List<Category> findAll() {
        List<Category> result = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list categories", e);
        }
    }

    public Category create(String name) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return new Category(keys.next() ? keys.getInt(1) : 0, name);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create category " + name, e);
        }
    }
}
