package com.supermart;

import com.supermart.config.Database;
import com.supermart.config.SchemaInitializer;

import java.util.concurrent.atomic.AtomicInteger;

/** Gives each test its own isolated, schema-initialised, seeded in-memory H2 database. */
public final class TestDatabases {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private TestDatabases() {
    }

    public static Database fresh() {
        Database database = Database.inMemory("supermart_test_" + COUNTER.incrementAndGet());
        SchemaInitializer.initialize(database);
        return database;
    }
}
