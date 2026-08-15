package com.supermart.dao;

import com.supermart.config.Database;
import com.supermart.model.Employee;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC persistence for employees. Every statement is parameterised - no string-built SQL. */
public class EmployeeDao {

    private static final String SELECT_BASE =
            "SELECT e.id, e.full_name, e.email, e.department_id, e.salary, e.joining_date, d.name AS department_name "
          + "FROM employees e JOIN departments d ON d.id = e.department_id";

    private final Database database;

    public EmployeeDao(Database database) {
        this.database = database;
    }

    public List<Employee> findAll() {
        List<Employee> result = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " ORDER BY e.full_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list employees", e);
        }
    }

    public Optional<Employee> findById(int id) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE e.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load employee " + id, e);
        }
    }

    /** Name search. The term is bound as a parameter even though it is wrapped in wildcards. */
    public List<Employee> searchByName(String term) {
        List<Employee> result = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE LOWER(e.full_name) LIKE ? ORDER BY e.full_name")) {
            ps.setString(1, "%" + term.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search employees", e);
        }
    }

    public Employee insert(Employee employee) {
        String sql = "INSERT INTO employees (full_name, email, department_id, salary, joining_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, employee);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    employee.setId(keys.getInt(1));
                }
            }
            return employee;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert employee", e);
        }
    }

    public boolean update(Employee employee) {
        String sql = "UPDATE employees SET full_name = ?, email = ?, department_id = ?, salary = ?, joining_date = ? WHERE id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, employee);
            ps.setInt(6, employee.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update employee " + employee.getId(), e);
        }
    }

    public boolean delete(int id) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete employee " + id, e);
        }
    }

    public int count() {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM employees");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count employees", e);
        }
    }

    private void bind(PreparedStatement ps, Employee employee) throws SQLException {
        ps.setString(1, employee.getFullName());
        ps.setString(2, employee.getEmail());
        ps.setInt(3, employee.getDepartmentId());
        ps.setBigDecimal(4, employee.getSalary());
        ps.setDate(5, Date.valueOf(employee.getJoiningDate()));
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setEmail(rs.getString("email"));
        employee.setDepartmentId(rs.getInt("department_id"));
        employee.setDepartmentName(rs.getString("department_name"));
        employee.setSalary(rs.getBigDecimal("salary"));
        employee.setJoiningDate(rs.getDate("joining_date").toLocalDate());
        return employee;
    }
}
