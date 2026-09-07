package com.ticketflow.user.user.domain.rules;

import com.ticketflow.user.user.domain.exception.format.UserEmailInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserNameInvalidException;
import com.ticketflow.user.user.domain.exception.format.UserPasswordInvalidException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRulesTest {

    @Test
    void nameRule_acceptsValidNames() {
        assertThatCode(() -> UserNameValidRule.validate("Diego Lopez")).doesNotThrowAnyException();
        assertThatCode(() -> UserNameValidRule.validate("Ana 123")).doesNotThrowAnyException();
    }

    @Test
    void nameRule_rejectsInvalidNames() {
        assertThatThrownBy(() -> UserNameValidRule.validate("Al")).isInstanceOf(UserNameInvalidException.class);
        assertThatThrownBy(() -> UserNameValidRule.validate(null)).isInstanceOf(UserNameInvalidException.class);
        assertThatThrownBy(() -> UserNameValidRule.validate("H@cker")).isInstanceOf(UserNameInvalidException.class);
    }

    @Test
    void emailRule_acceptsValidEmails() {
        assertThatCode(() -> UserEmailValidRule.validate("diego+tag@ticketflow.com")).doesNotThrowAnyException();
        assertThatCode(() -> UserEmailValidRule.validate("a.b_c@domain.co")).doesNotThrowAnyException();
    }

    @Test
    void emailRule_rejectsInvalidEmails() {
        assertThatThrownBy(() -> UserEmailValidRule.validate("not-an-email")).isInstanceOf(UserEmailInvalidException.class);
        assertThatThrownBy(() -> UserEmailValidRule.validate(null)).isInstanceOf(UserEmailInvalidException.class);
        assertThatThrownBy(() -> UserEmailValidRule.validate("")).isInstanceOf(UserEmailInvalidException.class);
    }

    @Test
    void passwordRule_acceptsValidPasswords() {
        assertThatCode(() -> UserPasswordValidRule.validate("Password123!")).doesNotThrowAnyException();
    }

    @Test
    void passwordRule_rejectsInvalidPasswords() {
        assertThatThrownBy(() -> UserPasswordValidRule.validate("short")).isInstanceOf(UserPasswordInvalidException.class);
        assertThatThrownBy(() -> UserPasswordValidRule.validate(null)).isInstanceOf(UserPasswordInvalidException.class);
    }
}