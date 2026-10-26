package com.ticketflow.user.user.domain.models;

import com.ticketflow.user.user.domain.models.enums.UserRole;
import com.ticketflow.user.user.domain.models.enums.UserStatus;
import com.ticketflow.user.user.domain.rules.UserNameValidRule;
import com.ticketflow.user.user.domain.rules.UserPhoneValidRule;
import com.ticketflow.user.user.domain.rules.UserRoleValidRule;
import com.ticketflow.user.user.domain.rules.UserValidatorRule;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class UserDomain {

    private final UUID id;
    private String name;
    private final String email;
    private String phone;
    private String password;
    private UserRole role;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserDomain(UUID id, String name, String email, String phone, String password, UserRole role,
                       UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "ID is mandatory");
        this.name = Objects.requireNonNull(name, "Name is mandatory");
        this.email = Objects.requireNonNull(email, "Email is mandatory");
        this.phone = phone;
        this.password = Objects.requireNonNull(password, "Password is mandatory");
        this.role = Objects.requireNonNull(role, "Role is mandatory");
        this.status = status == null ? UserStatus.defaultStatus() : status;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is mandatory");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt is mandatory");
    }

    private UserDomain(UUID id) {
        this.id = Objects.requireNonNull(id, "ID is mandatory");
        this.name = null;
        this.email = null;
        this.phone = null;
        this.password = null;
        this.role = null;
        this.status = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    public static UserDomain create(UUID id, String name, String email, String password, UserRole role) {
        return create(id, name, email, null, password, role);
    }

    public static UserDomain create(UUID id, String name, String email, String phone, String password, UserRole role) {
        LocalDateTime now = LocalDateTime.now();
        UserDomain user = new UserDomain(id, name, email, phone, password, role, UserStatus.defaultStatus(), now, now);
        return new UserValidatorRule().validate(user);
    }

    public static UserDomain create(UUID id) {
        return new UserDomain(id);
    }

    public static UserDomain from(UUID id, String name, String email, String password, UserRole role,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        return from(id, name, email, null, password, role, UserStatus.defaultStatus(), createdAt, updatedAt);
    }

    public static UserDomain from(UUID id, String name, String email, String phone, String password, UserRole role,
                                  UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new UserDomain(id, name, email, phone, password, role, status, createdAt, updatedAt);
    }

    public void changeRole(UserRole newRole) {
        UserRoleValidRule.validate(newRole);
        this.role = Objects.requireNonNull(newRole, "New role cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        this.password = Objects.requireNonNull(newPassword, "New password cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String newName, String newPhone) {
        UserNameValidRule.validate(newName);
        UserPhoneValidRule.validate(newPhone);
        this.name = newName;
        this.phone = UserPhoneValidRule.normalize(newPhone);
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "New status cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}