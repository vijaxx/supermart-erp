package com.supermart.service;

import com.supermart.TestDatabases;
import com.supermart.config.Database;
import com.supermart.dao.DepartmentDao;
import com.supermart.dao.EmployeeDao;
import com.supermart.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeServiceTest {

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        Database database = TestDatabases.fresh();
        employeeService = new EmployeeService(new EmployeeDao(database), new DepartmentDao(database));
    }

    private Employee validEmployee() {
        Employee employee = new Employee();
        employee.setFullName("Valid Person");
        employee.setEmail("valid.person@supermart.example");
        employee.setDepartmentId(1);
        employee.setSalary(new BigDecimal("40000"));
        employee.setJoiningDate(LocalDate.of(2023, 5, 1));
        return employee;
    }

    @Test
    void validEmployeeIsAccepted() {
        Employee created = employeeService.create(validEmployee());
        assertTrue(created.getId() > 0);
    }

    @Test
    void belowMinimumSalaryIsRejected() {
        Employee employee = validEmployee();
        employee.setSalary(new BigDecimal("500"));
        assertThrows(ValidationException.class, () -> employeeService.create(employee));
    }

    @Test
    void invalidEmailIsRejected() {
        Employee employee = validEmployee();
        employee.setEmail("not-an-email");
        assertThrows(ValidationException.class, () -> employeeService.create(employee));
    }

    @Test
    void futureJoiningDateIsRejected() {
        Employee employee = validEmployee();
        employee.setJoiningDate(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> employeeService.create(employee));
    }

    @Test
    void unknownDepartmentIsRejected() {
        Employee employee = validEmployee();
        employee.setDepartmentId(9999);
        assertThrows(ValidationException.class, () -> employeeService.create(employee));
    }

    @Test
    void blankNameIsRejected() {
        Employee employee = validEmployee();
        employee.setFullName("   ");
        assertThrows(ValidationException.class, () -> employeeService.create(employee));
    }

    @Test
    void creatingASecondEmployeeWithAnAlreadyUsedEmailIsRejected() {
        employeeService.create(validEmployee());

        Employee duplicate = validEmployee();
        duplicate.setFullName("Someone Else");
        assertThrows(ValidationException.class, () -> employeeService.create(duplicate));
    }

    @Test
    void updatingAnEmployeeToAnotherEmployeesEmailIsRejected() {
        Employee first = employeeService.create(validEmployee());

        Employee second = validEmployee();
        second.setEmail("second.person@supermart.example");
        second = employeeService.create(second);

        second.setEmail(first.getEmail());
        Employee toUpdate = second;
        assertThrows(ValidationException.class, () -> employeeService.update(toUpdate));
    }

    @Test
    void updatingAnEmployeeWithoutChangingTheirOwnEmailIsAccepted() {
        Employee created = employeeService.create(validEmployee());
        created.setSalary(new BigDecimal("45000"));
        assertTrue(employeeService.update(created));
    }
}
