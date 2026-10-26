package com.ticketflow.user.user.application.ports.out;

public interface SecureTokenPort {

    String generate();

    String hash(String token);
}
