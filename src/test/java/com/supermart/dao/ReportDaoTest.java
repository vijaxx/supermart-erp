package com.supermart.dao;

import com.supermart.TestDatabases;
import com.supermart.config.Database;
import com.supermart.model.report.CategoryValuationRow;
import com.supermart.model.report.DepartmentSalaryRow;
import com.supermart.model.report.LowStockRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the JOIN + GROUP BY reporting queries aggregate correctly against known seed data. */
class ReportDaoTest {

    private ReportDao reportDao;
    private EmployeeDao employeeDao;

    @BeforeEach
    void setUp() {
        Database database = TestDatabases.fresh();
        reportDao = new ReportDao(database);
        employeeDao = new EmployeeDao(database);
    }

    @Test
    void departmentSalaryReportCoversEveryDepartment() {
        List<DepartmentSalaryRow> rows = reportDao.departmentSalaryReport();
        // Seed data has 4 departments (Store Operations, Warehouse, Finance, IT).
        assertEquals(4, rows.size());
    }

    @Test
    void departmentSalaryReportSumMatchesIndividualSalaries() {
        BigDecimal expectedTotal = employeeDao.findAll().stream()
                .map(e -> e.getSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal reportedTotal = reportDao.departmentSalaryReport().stream()
                .map(DepartmentSalaryRow::getTotalSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, expectedTotal.compareTo(reportedTotal),
                "sum of per-department totals must equal the sum of every employee's salary");
    }

    @Test
    void departmentSalaryReportHeadcountMatchesEmployeeCount() {
        int totalHeadcount = reportDao.departmentSalaryReport().stream()
                .mapToInt(DepartmentSalaryRow::getHeadcount)
                .sum();
        assertEquals(employeeDao.count(), totalHeadcount);
    }

    @Test
    void categoryValuationReportValueMatchesPriceTimesStock() {
        List<CategoryValuationRow> rows = reportDao.categoryValuationReport();
        BigDecimal totalFromReport = rows.stream()
                .map(CategoryValuationRow::getInventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalFromReport.compareTo(reportDao.totalInventoryValue()));
    }

    @Test
    void lowStockReportOnlyIncludesProductsAtOrBelowThreshold() {
        List<LowStockRow> rows = reportDao.lowStockReport();
        assertTrue(rows.stream().allMatch(r -> r.getStockQuantity() <= r.getReorderLevel()));
        assertTrue(rows.stream().anyMatch(r -> r.getSku().equals("BEV-002")));
    }

    @Test
    void lowStockRowShortfallIsNonNegative() {
        List<LowStockRow> rows = reportDao.lowStockReport();
        assertTrue(rows.stream().allMatch(r -> r.getShortfall() >= 0));
    }
}
