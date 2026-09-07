package com.ticketflow.user.user.domain.models;

import com.ticketflow.user.user.domain.models.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDomainTest {

    @Test
    void create_validUser_returnsPersistentUser() {
        UserDomain user = UserDomain.create(
                UUID.randomUUID(),
                "Diego López",
                "diego@ticketflow.com",
                "$argon2id$hash-here",
                UserRole.BUYER
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("Diego López");
        assertThat(user.getEmail()).isEqualTo("diego@ticketflow.com");
        assertThat(user.getRole()).isEqualTo(UserRole.BUYER);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void create_missingName_throwsNullPointer() {
        assertThatThrownBy(() -> UserDomain.create(
                UUID.randomUUID(),
                null,
                "diego@ticketflow.com",
                "hash",
                UserRole.BUYER
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void changeRole_updatesRoleAndTimestamp() {
        UserDomain user = UserDomain.create(
                UUID.randomUUID(), "Diego", "diego@ticketflow.com", "hash", UserRole.BUYER);
        var before = user.getUpdatedAt();

        user.changeRole(UserRole.ORGANIZER);

        assertThat(user.getRole()).isEqualTo(UserRole.ORGANIZER);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void changePassword_updatesPasswordHash() {
        UserDomain user = UserDomain.create(
                UUID.randomUUID(), "Diego", "diego@ticketflow.com", "old-hash", UserRole.BUYER);

        user.changePassword("new-hash");

        assertThat(user.getPassword()).isEqualTo("new-hash");
    }
}