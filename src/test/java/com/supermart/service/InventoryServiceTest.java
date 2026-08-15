package com.supermart.service;

import com.supermart.TestDatabases;
import com.supermart.config.Database;
import com.supermart.dao.CategoryDao;
import com.supermart.dao.ProductDao;
import com.supermart.dao.SupplierDao;
import com.supermart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryServiceTest {

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        Database database = TestDatabases.fresh();
        inventoryService = new InventoryService(new ProductDao(database), new CategoryDao(database), new SupplierDao(database));
    }

    @Test
    void lowStockReturnsOnlyItemsAtOrBelowReorderLevel() {
        List<Product> lowStock = inventoryService.lowStock();
        assertFalse(lowStock.isEmpty());
        assertTrue(lowStock.stream().allMatch(Product::isLowStock));
    }

    @Test
    void lowStockCountMatchesListSize() {
        assertEquals(inventoryService.lowStock().size(), inventoryService.lowStockCount());
    }

    @Test
    void stockAtExactlyReorderLevelCountsAsLowStock() {
        Product product = new Product();
        product.setName("Boundary Item");
        product.setSku("BND-001");
        product.setCategoryId(1);
        product.setSupplierId(1);
        product.setUnitPrice(new BigDecimal("5.00"));
        product.setStockQuantity(20);
        product.setReorderLevel(20);
        Product created = inventoryService.create(product);
        assertTrue(inventoryService.find(created.getId()).orElseThrow().isLowStock());
    }

    @Test
    void adjustingStockDownwardBeyondZeroIsRejected() {
        Product product = inventoryService.listAll().get(0);
        int onHand = product.getStockQuantity();
        assertThrows(ValidationException.class, () -> inventoryService.adjustStock(product.getId(), -(onHand + 1)));
    }

    @Test
    void adjustingStockWithinBoundsSucceeds() {
        Product product = inventoryService.listAll().get(0);
        int onHand = product.getStockQuantity();
        Product updated = inventoryService.adjustStock(product.getId(), -onHand);
        assertEquals(0, updated.getStockQuantity());
    }

    @Test
    void creatingProductWithZeroPriceIsRejected() {
        Product product = new Product();
        product.setName("Bad Product");
        product.setSku("BAD-001");
        product.setCategoryId(1);
        product.setSupplierId(1);
        product.setUnitPrice(BigDecimal.ZERO);
        product.setStockQuantity(10);
        product.setReorderLevel(5);
        assertThrows(ValidationException.class, () -> inventoryService.create(product));
    }

    @Test
    void creatingProductWithoutCategoryIsRejected() {
        Product product = new Product();
        product.setName("No Category");
        product.setSku("NC-001");
        product.setCategoryId(0);
        product.setSupplierId(1);
        product.setUnitPrice(BigDecimal.TEN);
        product.setStockQuantity(10);
        product.setReorderLevel(5);
        assertThrows(ValidationException.class, () -> inventoryService.create(product));
    }
}
