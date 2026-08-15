package com.supermart.model.report;

import java.math.BigDecimal;

/** One row of the "inventory valuation by category" report (JOIN + GROUP BY). */
public class CategoryValuationRow {
    private final String categoryName;
    private final int productCount;
    private final int totalUnits;
    private final BigDecimal inventoryValue;

    public CategoryValuationRow(String categoryName, int productCount, int totalUnits, BigDecimal inventoryValue) {
        this.categoryName = categoryName;
        this.productCount = productCount;
        this.totalUnits = totalUnits;
        this.inventoryValue = inventoryValue;
    }

    public String getCategoryName() { return categoryName; }
    public int getProductCount() { return productCount; }
    public int getTotalUnits() { return totalUnits; }
    public BigDecimal getInventoryValue() { return inventoryValue; }
}
