package com.supermart.model.report;

import java.math.BigDecimal;

/** One row of the "employees per department" report (JOIN + GROUP BY). */
public class DepartmentSalaryRow {
    private final String departmentName;
    private final int headcount;
    private final BigDecimal totalSalary;
    private final BigDecimal averageSalary;
    private final BigDecimal maxSalary;

    public DepartmentSalaryRow(String departmentName, int headcount, BigDecimal totalSalary,
                               BigDecimal averageSalary, BigDecimal maxSalary) {
        this.departmentName = departmentName;
        this.headcount = headcount;
        this.totalSalary = totalSalary;
        this.averageSalary = averageSalary;
        this.maxSalary = maxSalary;
    }

    public String getDepartmentName() { return departmentName; }
    public int getHeadcount() { return headcount; }
    public BigDecimal getTotalSalary() { return totalSalary; }
    public BigDecimal getAverageSalary() { return averageSalary; }
    public BigDecimal getMaxSalary() { return maxSalary; }
}
