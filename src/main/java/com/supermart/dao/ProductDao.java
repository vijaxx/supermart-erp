package com.supermart.dao;

import com.supermart.config.Database;
import com.supermart.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC persistence for inventory products, joined to their category and supplier. */
public class ProductDao {

    private static final String SELECT_BASE =
            "SELECT p.id, p.name, p.sku, p.category_id, p.supplier_id, p.unit_price, p.stock_quantity, "
          + "       p.reorder_level, c.name AS category_name, s.name AS supplier_name "
          + "FROM products p "
          + "JOIN categories c ON c.id = p.category_id "
          + "JOIN suppliers  s ON s.id = p.supplier_id";

    private final Database database;

    public ProductDao(Database database) {
        this.database = database;
    }

    public List<Product> findAll() {
        return query(SELECT_BASE + " ORDER BY p.name");
    }

    /** Products at or below their reorder threshold - the low-stock rule, expressed in SQL. */
    public List<Product> findLowStock() {
        return query(SELECT_BASE + " WHERE p.stock_quantity <= p.reorder_level ORDER BY (p.reorder_level - p.stock_quantity) DESC");
    }

    public List<Product> findByCategory(int categoryId) {
        List<Product> result = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE p.category_id = ? ORDER BY p.name")) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list products for category " + categoryId, e);
        }
    }

    public Optional<Product> findById(int id) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load product " + id, e);
        }
    }

    public Product insert(Product product) {
        String sql = "INSERT INTO products (name, sku, category_id, supplier_id, unit_price, stock_quantity, reorder_level) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, product);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert product", e);
        }
    }

    public boolean update(Product product) {
        String sql = "UPDATE products SET name = ?, sku = ?, category_id = ?, supplier_id = ?, unit_price = ?, "
                   + "stock_quantity = ?, reorder_level = ? WHERE id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, product);
            ps.setInt(8, product.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update product " + product.getId(), e);
        }
    }

    /**
     * Relative stock movement (positive = goods received, negative = sold/written off).
     * The {@code stock_quantity + ? >= 0} guard makes this atomic: two concurrent calls
     * that would individually be safe based on a stale read can no longer race each
     * other into a negative balance, since the check and the write happen in one
     * statement instead of the caller reading, deciding, then writing separately.
     */
    public boolean adjustStock(int productId, int delta) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE products SET stock_quantity = stock_quantity + ? "
                   + "WHERE id = ? AND stock_quantity + ? >= 0")) {
            ps.setInt(1, delta);
            ps.setInt(2, productId);
            ps.setInt(3, delta);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to adjust stock for product " + productId, e);
        }
    }

    public boolean delete(int id) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete product " + id, e);
        }
    }

    public int count() {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count products", e);
        }
    }

    private List<Product> query(String sql) {
        List<Product> result = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list products", e);
        }
    }

    private void bind(PreparedStatement ps, Product product) throws SQLException {
        ps.setString(1, product.getName());
        ps.setString(2, product.getSku());
        ps.setInt(3, product.getCategoryId());
        ps.setInt(4, product.getSupplierId());
        ps.setBigDecimal(5, product.getUnitPrice());
        ps.setInt(6, product.getStockQuantity());
        ps.setInt(7, product.getReorderLevel());
    }

    private Product map(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setSku(rs.getString("sku"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setSupplierId(rs.getInt("supplier_id"));
        product.setSupplierName(rs.getString("supplier_name"));
        product.setUnitPrice(rs.getBigDecimal("unit_price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        return product;
    }
}
