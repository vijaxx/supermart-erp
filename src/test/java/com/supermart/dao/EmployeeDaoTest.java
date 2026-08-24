package com.supermart.dao;

import com.supermart.TestDatabases;
import com.supermart.config.Database;
import com.supermart.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeDaoTest {

    private EmployeeDao employeeDao;

    @BeforeEach
    void setUp() {
        Database database = TestDatabases.fresh();
        employeeDao = new EmployeeDao(database);
    }

    @Test
    void seedDataLoadsSevenEmployees() {
        assertEquals(7, employeeDao.count());
    }

    @Test
    void findAllJoinsDepartmentName() {
        List<Employee> employees = employeeDao.findAll();
        assertTrue(employees.stream().allMatch(e -> e.getDepartmentName() != null && !e.getDepartmentName().isBlank()));
    }

    @Test
    void insertThenFindByIdRoundTrips() {
        Employee employee = new Employee();
        employee.setFullName("Test Employee");
        employee.setEmail("test.employee@supermart.example");
        employee.setDepartmentId(1);
        employee.setSalary(new BigDecimal("50000.00"));
        employee.setJoiningDate(LocalDate.of(2024, 1, 15));

        Employee inserted = employeeDao.insert(employee);
        assertTrue(inserted.getId() > 0);

        Optional<Employee> found = employeeDao.findById(inserted.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Employee", found.get().getFullName());
        assertEquals(new BigDecimal("50000.00"), found.get().getSalary());
        assertEquals(8, employeeDao.count());
    }

    @Test
    void updateChangesStoredFields() {
        Employee employee = employeeDao.findAll().get(0);
        employee.setSalary(new BigDecimal("99999.99"));
        boolean updated = employeeDao.update(employee);
        assertTrue(updated);

        Employee reloaded = employeeDao.findById(employee.getId()).orElseThrow();
        assertEquals(new BigDecimal("99999.99"), reloaded.getSalary());
    }

    @Test
    void deleteRemovesTheRow() {
        Employee employee = employeeDao.findAll().get(0);
        boolean deleted = employeeDao.delete(employee.getId());
        assertTrue(deleted);
        assertFalse(employeeDao.findById(employee.getId()).isPresent());
        assertEquals(6, employeeDao.count());
    }

    @Test
    void searchByNameIsCaseInsensitiveAndPartial() {
        List<Employee> results = employeeDao.searchByName("anita");
        assertEquals(1, results.size());
        assertEquals("Anita Rao", results.get(0).getFullName());
    }

    @Test
    void searchByNameTreatsUnderscoreAsALiteralCharacterNotAWildcard() {
        Employee withUnderscore = new Employee();
        withUnderscore.setFullName("Priya_Test");
        withUnderscore.setEmail("priya.underscore@supermart.example");
        withUnderscore.setDepartmentId(1);
        withUnderscore.setSalary(new BigDecimal("50000.00"));
        withUnderscore.setJoiningDate(LocalDate.of(2024, 1, 15));
        employeeDao.insert(withUnderscore);

        Employee withoutUnderscore = new Employee();
        withoutUnderscore.setFullName("PriyaXTest");
        withoutUnderscore.setEmail("priya.plain@supermart.example");
        withoutUnderscore.setDepartmentId(1);
        withoutUnderscore.setSalary(new BigDecimal("50000.00"));
        withoutUnderscore.setJoiningDate(LocalDate.of(2024, 1, 15));
        employeeDao.insert(withoutUnderscore);

        // Before the LIKE-escaping fix, "_" matched any single character, so
        // this search would have returned both employees instead of just the
        // one whose name genuinely contains an underscore.
        List<Employee> results = employeeDao.searchByName("priya_test");
        assertEquals(1, results.size());
        assertEquals("Priya_Test", results.get(0).getFullName());
    }
}
