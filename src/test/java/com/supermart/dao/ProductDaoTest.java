package com.supermart.dao;

import com.supermart.TestDatabases;
import com.supermart.config.Database;
import com.supermart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDaoTest {

    private ProductDao productDao;

    @BeforeEach
    void setUp() {
        Database database = TestDatabases.fresh();
        productDao = new ProductDao(database);
    }

    @Test
    void seedDataLoadsTenProducts() {
        assertEquals(10, productDao.count());
    }

    @Test
    void findAllJoinsCategoryAndSupplierNames() {
        List<Product> products = productDao.findAll();
        assertTrue(products.stream().allMatch(p -> p.getCategoryName() != null && p.getSupplierName() != null));
    }

    @Test
    void findLowStockOnlyReturnsProductsAtOrBelowReorderLevel() {
        List<Product> lowStock = productDao.findLowStock();
        assertFalse(lowStock.isEmpty());
        assertTrue(lowStock.stream().allMatch(p -> p.getStockQuantity() <= p.getReorderLevel()));
        // Masala Chai (22 in stock, reorder 30) is seeded low; Filter Coffee (140/40) is not.
        assertTrue(lowStock.stream().anyMatch(p -> p.getSku().equals("BEV-002")));
        assertFalse(lowStock.stream().anyMatch(p -> p.getSku().equals("BEV-001")));
    }

    @Test
    void insertThenFindByIdRoundTrips() {
        Product product = new Product();
        product.setName("Test Widget");
        product.setSku("TST-999");
        product.setCategoryId(1);
        product.setSupplierId(1);
        product.setUnitPrice(new BigDecimal("10.00"));
        product.setStockQuantity(5);
        product.setReorderLevel(2);

        Product inserted = productDao.insert(product);
        assertTrue(inserted.getId() > 0);

        Optional<Product> found = productDao.findById(inserted.getId());
        assertTrue(found.isPresent());
        assertEquals("TST-999", found.get().getSku());
        assertEquals(11, productDao.count());
    }

    @Test
    void adjustStockAppliesRelativeDelta() {
        Product product = productDao.findAll().get(0);
        int before = product.getStockQuantity();
        productDao.adjustStock(product.getId(), 15);
        Product reloaded = productDao.findById(product.getId()).orElseThrow();
        assertEquals(before + 15, reloaded.getStockQuantity());
    }

    @Test
    void adjustStockGuardsAgainstGoingNegativeAtomically() {
        // Unlike InventoryService's pre-check, this exercises the DAO's own SQL guard
        // directly: even without a caller-side check, the UPDATE itself must refuse to
        // drive stock_quantity below zero, and report the row as unchanged when it does.
        Product product = productDao.findAll().get(0);
        int before = product.getStockQuantity();

        boolean applied = productDao.adjustStock(product.getId(), -(before + 1));
        assertFalse(applied, "guarded update must reject going negative");

        Product reloaded = productDao.findById(product.getId()).orElseThrow();
        assertEquals(before, reloaded.getStockQuantity(), "stock must be unchanged when the guard rejects the update");
    }

    @Test
    void deleteRemovesTheRow() {
        Product product = productDao.findAll().get(0);
        assertTrue(productDao.delete(product.getId()));
        assertFalse(productDao.findById(product.getId()).isPresent());
        assertEquals(9, productDao.count());
    }

    @Test
    void findByCategoryFiltersCorrectly() {
        List<Product> beverages = productDao.findByCategory(1);
        assertTrue(beverages.stream().allMatch(p -> p.getCategoryId() == 1));
        assertFalse(beverages.isEmpty());
    }
}
