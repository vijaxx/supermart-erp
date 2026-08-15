package com.supermart.service;

import com.supermart.dao.ReportDao;
import com.supermart.model.report.CategoryValuationRow;
import com.supermart.model.report.DepartmentSalaryRow;
import com.supermart.model.report.LowStockRow;

import java.math.BigDecimal;
import java.util.List;

/** Admin-only reporting. All aggregation is delegated to JOIN + GROUP BY SQL in {@link ReportDao}. */
public class ReportService {

    private final ReportDao reportDao;

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public List<DepartmentSalaryRow> departmentSalaries() {
        return reportDao.departmentSalaryReport();
    }

    public List<CategoryValuationRow> categoryValuations() {
        return reportDao.categoryValuationReport();
    }

    public List<LowStockRow> lowStock() {
        return reportDao.lowStockReport();
    }

    public BigDecimal totalInventoryValue() {
        return reportDao.totalInventoryValue();
    }

    public BigDecimal totalPayroll() {
        return reportDao.totalPayroll();
    }
}
