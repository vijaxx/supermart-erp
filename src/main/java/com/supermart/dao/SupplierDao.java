package com.supermart.dao;

import com.supermart.config.Database;
import com.supermart.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierDao {

    private final Database database;

    public SupplierDao(Database database) {
        this.database = database;
    }

    public List<Supplier> findAll() {
        List<Supplier> result = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, name, contact_email FROM suppliers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Supplier(rs.getInt("id"), rs.getString("name"), rs.getString("contact_email")));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list suppliers", e);
        }
    }

    public Supplier create(String name, String contactEmail) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO suppliers (name, contact_email) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, contactEmail);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return new Supplier(keys.next() ? keys.getInt(1) : 0, name, contactEmail);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create supplier " + name, e);
        }
    }
}
