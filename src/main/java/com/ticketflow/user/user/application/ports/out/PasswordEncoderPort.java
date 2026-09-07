package com.ticketflow.user.user.application.ports.out;

public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}