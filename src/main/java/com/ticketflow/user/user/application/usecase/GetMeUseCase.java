package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.service.GetMePort;
import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMeUseCase implements GetMePort {

    private final UserRepositoryPort userRepository;

    @Override
    public GetUserResponseDTO execute(UUID userId) {
        return userRepository.findById(userId)
                .map(GetUserResponseDTO::fromDomain)
                .orElseThrow(UserNotFoundException::new);
    }
}