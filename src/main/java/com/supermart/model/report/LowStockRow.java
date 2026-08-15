package com.supermart.model.report;

/** One row of the low-stock report (products JOIN categories JOIN suppliers). */
public class LowStockRow {
    private final String productName;
    private final String sku;
    private final String categoryName;
    private final String supplierName;
    private final String supplierEmail;
    private final int stockQuantity;
    private final int reorderLevel;

    public LowStockRow(String productName, String sku, String categoryName, String supplierName,
                       String supplierEmail, int stockQuantity, int reorderLevel) {
        this.productName = productName;
        this.sku = sku;
        this.categoryName = categoryName;
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
    }

    public String getProductName() { return productName; }
    public String getSku() { return sku; }
    public String getCategoryName() { return categoryName; }
    public String getSupplierName() { return supplierName; }
    public String getSupplierEmail() { return supplierEmail; }
    public int getStockQuantity() { return stockQuantity; }
    public int getReorderLevel() { return reorderLevel; }

    /** How many units short of the reorder threshold this product is. */
    public int getShortfall() { return Math.max(0, reorderLevel - stockQuantity); }
}
