package com.supermart.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private String sku;
    private int categoryId;
    private String categoryName; // populated by JOIN queries
    private int supplierId;
    private String supplierName; // populated by JOIN queries
    private BigDecimal unitPrice;
    private int stockQuantity;
    private int reorderLevel;

    public Product() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    /** Business rule: stock at or below the reorder threshold needs replenishment. */
    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
    }

    public BigDecimal getStockValue() {
        return unitPrice == null ? BigDecimal.ZERO : unitPrice.multiply(BigDecimal.valueOf(stockQuantity));
    }
}
