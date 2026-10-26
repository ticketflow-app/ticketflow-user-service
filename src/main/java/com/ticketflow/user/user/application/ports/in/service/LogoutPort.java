package com.ticketflow.user.user.application.ports.in.service;

import java.util.UUID;

public interface LogoutPort {
    void logout(UUID userId);
}
