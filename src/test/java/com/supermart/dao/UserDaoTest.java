package com.supermart.dao;

import com.supermart.TestDatabases;
import com.supermart.config.Database;
import com.supermart.model.Role;
import com.supermart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        Database database = TestDatabases.fresh();
        userDao = new UserDao(database);
    }

    @Test
    void seedDataCreatesAdminAndStaffLogins() {
        assertTrue(userDao.findByUsername("admin").isPresent());
        assertTrue(userDao.findByUsername("staff").isPresent());
        assertEquals(2, userDao.count());
    }

    @Test
    void seededAdminHasAdminRole() {
        User admin = userDao.findByUsername("admin").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole());
    }

    @Test
    void seededStaffHasStaffRole() {
        User staff = userDao.findByUsername("staff").orElseThrow();
        assertEquals(Role.STAFF, staff.getRole());
    }

    @Test
    void unknownUsernameReturnsEmpty() {
        Optional<User> result = userDao.findByUsername("does-not-exist");
        assertTrue(result.isEmpty());
    }

    @Test
    void createPersistsANewUser() {
        User created = userDao.create("newhire", "New Hire", "pbkdf2$1$AA$AA", Role.STAFF);
        assertTrue(created.getId() > 0);
        assertTrue(userDao.findByUsername("newhire").isPresent());
        assertEquals(3, userDao.count());
    }
}
