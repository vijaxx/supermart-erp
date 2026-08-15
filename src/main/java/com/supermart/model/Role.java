package com.supermart.model;

/** Application roles. ADMIN may manage employees and see reports; STAFF is inventory-only. */
public enum Role {
    ADMIN,
    STAFF;

    public static Role of(String raw) {
        return raw == null ? STAFF : Role.valueOf(raw.trim().toUpperCase());
    }
}
