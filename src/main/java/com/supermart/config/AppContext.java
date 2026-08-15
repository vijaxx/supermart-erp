package com.supermart.config;

import com.supermart.dao.CategoryDao;
import com.supermart.dao.DepartmentDao;
import com.supermart.dao.EmployeeDao;
import com.supermart.dao.ProductDao;
import com.supermart.dao.ReportDao;
import com.supermart.dao.SupplierDao;
import com.supermart.dao.UserDao;
import com.supermart.service.AuthService;
import com.supermart.service.EmployeeService;
import com.supermart.service.InventoryService;
import com.supermart.service.ReportService;

import jakarta.servlet.ServletContext;

/**
 * Poor-man's dependency injection: one place that wires DAOs onto a {@link Database} and services
 * onto DAOs. Servlets pull the ready-made services out of the {@code ServletContext}, which keeps
 * the controller layer free of persistence details.
 */
public class AppContext {

    public static final String ATTRIBUTE = "appContext";

    private final Database database;
    private final AuthService authService;
    private final EmployeeService employeeService;
    private final InventoryService inventoryService;
    private final ReportService reportService;

    public AppContext(Database database) {
        this.database = database;
        UserDao userDao = new UserDao(database);
        EmployeeDao employeeDao = new EmployeeDao(database);
        DepartmentDao departmentDao = new DepartmentDao(database);
        ProductDao productDao = new ProductDao(database);
        CategoryDao categoryDao = new CategoryDao(database);
        SupplierDao supplierDao = new SupplierDao(database);
        ReportDao reportDao = new ReportDao(database);

        this.authService = new AuthService(userDao);
        this.employeeService = new EmployeeService(employeeDao, departmentDao);
        this.inventoryService = new InventoryService(productDao, categoryDao, supplierDao);
        this.reportService = new ReportService(reportDao);
    }

    public static AppContext from(ServletContext servletContext) {
        AppContext context = (AppContext) servletContext.getAttribute(ATTRIBUTE);
        if (context == null) {
            throw new IllegalStateException("Application context was not initialised");
        }
        return context;
    }

    public Database getDatabase() { return database; }
    public AuthService getAuthService() { return authService; }
    public EmployeeService getEmployeeService() { return employeeService; }
    public InventoryService getInventoryService() { return inventoryService; }
    public ReportService getReportService() { return reportService; }
}
