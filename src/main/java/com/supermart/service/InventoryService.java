package com.supermart.service;

import com.supermart.dao.CategoryDao;
import com.supermart.dao.ProductDao;
import com.supermart.dao.SupplierDao;
import com.supermart.model.Category;
import com.supermart.model.Product;
import com.supermart.model.Supplier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Inventory business rules, including the low-stock policy. */
public class InventoryService {

    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final SupplierDao supplierDao;

    public InventoryService(ProductDao productDao, CategoryDao categoryDao, SupplierDao supplierDao) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
        this.supplierDao = supplierDao;
    }

    public List<Product> listAll() {
        return productDao.findAll();
    }

    public List<Product> listByCategory(int categoryId) {
        return productDao.findByCategory(categoryId);
    }

    public Optional<Product> find(int id) {
        return productDao.findById(id);
    }

    public List<Category> categories() {
        return categoryDao.findAll();
    }

    public List<Supplier> suppliers() {
        return supplierDao.findAll();
    }

    /** Products whose stock has fallen to or below the reorder threshold. */
    public List<Product> lowStock() {
        return productDao.findLowStock();
    }

    public int lowStockCount() {
        return productDao.findLowStock().size();
    }

    public Product create(Product product) {
        validate(product);
        return productDao.insert(product);
    }

    public boolean update(Product product) {
        if (product.getId() <= 0) {
            throw new ValidationException("Product id is required for an update");
        }
        validate(product);
        return productDao.update(product);
    }

    public boolean delete(int id) {
        return productDao.delete(id);
    }

    /**
     * Apply a stock movement. Stock may never be driven negative - selling more than is on hand is
     * rejected rather than silently clamped.
     */
    public Product adjustStock(int productId, int delta) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ValidationException("Unknown product"));
        if (delta == 0) {
            return product;
        }
        if (product.getStockQuantity() + delta < 0) {
            throw new ValidationException("Stock cannot go negative: only "
                    + product.getStockQuantity() + " units of " + product.getName() + " on hand");
        }
        productDao.adjustStock(productId, delta);
        return productDao.findById(productId).orElseThrow();
    }

    public int productCount() {
        return productDao.count();
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ValidationException("Product name is required");
        }
        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new ValidationException("SKU is required");
        }
        if (product.getUnitPrice() == null || product.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Unit price must be greater than zero");
        }
        if (product.getStockQuantity() < 0) {
            throw new ValidationException("Stock quantity cannot be negative");
        }
        if (product.getReorderLevel() < 0) {
            throw new ValidationException("Reorder level cannot be negative");
        }
        if (product.getCategoryId() <= 0 || product.getSupplierId() <= 0) {
            throw new ValidationException("Category and supplier are required");
        }
    }
}
