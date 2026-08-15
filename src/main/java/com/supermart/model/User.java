package com.supermart.model;

/** Application login. The password is never held in plaintext - only salt + PBKDF2 hash. */
public class User {
    private int id;
    private String username;
    private String fullName;
    private String passwordHash;
    private Role role;

    public User() {
    }

    public User(int id, String username, String fullName, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isAdmin() { return role == Role.ADMIN; }
}
