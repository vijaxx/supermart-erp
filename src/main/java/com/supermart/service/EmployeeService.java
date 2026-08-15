package com.supermart.service;

import com.supermart.dao.DepartmentDao;
import com.supermart.dao.EmployeeDao;
import com.supermart.model.Department;
import com.supermart.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Employee business rules: validation lives here, not in the servlet and not in the DAO. */
public class EmployeeService {

    private static final BigDecimal MINIMUM_SALARY = new BigDecimal("10000");

    private final EmployeeDao employeeDao;
    private final DepartmentDao departmentDao;

    public EmployeeService(EmployeeDao employeeDao, DepartmentDao departmentDao) {
        this.employeeDao = employeeDao;
        this.departmentDao = departmentDao;
    }

    public List<Employee> listAll() {
        return employeeDao.findAll();
    }

    public List<Employee> search(String term) {
        return term == null || term.isBlank() ? employeeDao.findAll() : employeeDao.searchByName(term.trim());
    }

    public Optional<Employee> find(int id) {
        return employeeDao.findById(id);
    }

    public List<Department> departments() {
        return departmentDao.findAll();
    }

    public Employee create(Employee employee) {
        validate(employee);
        return employeeDao.insert(employee);
    }

    public boolean update(Employee employee) {
        if (employee.getId() <= 0) {
            throw new ValidationException("Employee id is required for an update");
        }
        validate(employee);
        return employeeDao.update(employee);
    }

    public boolean delete(int id) {
        return employeeDao.delete(id);
    }

    public int headcount() {
        return employeeDao.count();
    }

    private void validate(Employee employee) {
        if (employee.getFullName() == null || employee.getFullName().isBlank()) {
            throw new ValidationException("Full name is required");
        }
        if (employee.getEmail() == null || !employee.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("A valid email address is required");
        }
        if (employee.getSalary() == null || employee.getSalary().compareTo(MINIMUM_SALARY) < 0) {
            throw new ValidationException("Salary must be at least " + MINIMUM_SALARY);
        }
        if (employee.getJoiningDate() == null || employee.getJoiningDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Joining date must be today or in the past");
        }
        if (departmentDao.findById(employee.getDepartmentId()).isEmpty()) {
            throw new ValidationException("Unknown department");
        }
    }
}
