package com.supermart.dao;

import com.supermart.config.Database;
import com.supermart.model.report.CategoryValuationRow;
import com.supermart.model.report.DepartmentSalaryRow;
import com.supermart.model.report.LowStockRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin reporting queries. These are the multi-table JOIN + GROUP BY statements - the aggregation
 * happens in the database, not by looping over rows in Java.
 */
public class ReportDao {

    /** departments LEFT JOIN employees, aggregated per department (empty departments still show). */
    private static final String DEPARTMENT_SALARY_SQL =
            "SELECT d.name                       AS department_name, "
          + "       COUNT(e.id)                  AS headcount, "
          + "       COALESCE(SUM(e.salary), 0)   AS total_salary, "
          + "       COALESCE(AVG(e.salary), 0)   AS average_salary, "
          + "       COALESCE(MAX(e.salary), 0)   AS max_salary "
          + "FROM departments d "
          + "LEFT JOIN employees e ON e.department_id = d.id "
          + "GROUP BY d.id, d.name "
          + "ORDER BY COALESCE(SUM(e.salary), 0) DESC, d.name";

    /** categories LEFT JOIN products, valuing stock on hand per category. */
    private static final String CATEGORY_VALUATION_SQL =
            "SELECT c.name                                          AS category_name, "
          + "       COUNT(p.id)                                     AS product_count, "
          + "       COALESCE(SUM(p.stock_quantity), 0)              AS total_units, "
          + "       COALESCE(SUM(p.unit_price * p.stock_quantity), 0) AS inventory_value "
          + "FROM categories c "
          + "LEFT JOIN products p ON p.category_id = c.id "
          + "GROUP BY c.id, c.name "
          + "ORDER BY COALESCE(SUM(p.unit_price * p.stock_quantity), 0) DESC, c.name";

    /** Three-table JOIN: which products need reordering, and who to reorder them from. */
    private static final String LOW_STOCK_SQL =
            "SELECT p.name AS product_name, p.sku, c.name AS category_name, "
          + "       s.name AS supplier_name, s.contact_email AS supplier_email, "
          + "       p.stock_quantity, p.reorder_level "
          + "FROM products p "
          + "JOIN categories c ON c.id = p.category_id "
          + "JOIN suppliers  s ON s.id = p.supplier_id "
          + "WHERE p.stock_quantity <= p.reorder_level "
          + "ORDER BY (p.reorder_level - p.stock_quantity) DESC, p.name";

    private final Database database;

    public ReportDao(Database database) {
        this.database = database;
    }

    public List<DepartmentSalaryRow> departmentSalaryReport() {
        List<DepartmentSalaryRow> rows = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(DEPARTMENT_SALARY_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new DepartmentSalaryRow(
                        rs.getString("department_name"),
                        rs.getInt("headcount"),
                        rs.getBigDecimal("total_salary"),
                        rs.getBigDecimal("average_salary"),
                        rs.getBigDecimal("max_salary")));
            }
            return rows;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to build the department salary report", e);
        }
    }

    public List<CategoryValuationRow> categoryValuationReport() {
        List<CategoryValuationRow> rows = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(CATEGORY_VALUATION_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new CategoryValuationRow(
                        rs.getString("category_name"),
                        rs.getInt("product_count"),
                        rs.getInt("total_units"),
                        rs.getBigDecimal("inventory_value")));
            }
            return rows;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to build the inventory valuation report", e);
        }
    }

    public List<LowStockRow> lowStockReport() {
        List<LowStockRow> rows = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(LOW_STOCK_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new LowStockRow(
                        rs.getString("product_name"),
                        rs.getString("sku"),
                        rs.getString("category_name"),
                        rs.getString("supplier_name"),
                        rs.getString("supplier_email"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("reorder_level")));
            }
            return rows;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to build the low-stock report", e);
        }
    }

    public BigDecimal totalInventoryValue() {
        String sql = "SELECT COALESCE(SUM(p.unit_price * p.stock_quantity), 0) AS total FROM products p";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to total the inventory value", e);
        }
    }

    public BigDecimal totalPayroll() {
        String sql = "SELECT COALESCE(SUM(salary), 0) AS total FROM employees";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to total the payroll", e);
        }
    }
}
