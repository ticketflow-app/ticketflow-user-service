package com.ticketflow.user.user.application.ports.in.service;

import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestDTO;
import com.ticketflow.user.user.application.ports.in.dto.OrganizerRequestResponseDTO;

import java.util.UUID;

public interface RequestOrganizerPort {
    OrganizerRequestResponseDTO request(UUID userId, OrganizerRequestDTO request);
}
