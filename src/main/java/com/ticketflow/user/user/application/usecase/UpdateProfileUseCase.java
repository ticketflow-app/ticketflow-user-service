package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.dto.UpdateProfileRequestDTO;
import com.ticketflow.user.user.application.ports.in.service.UpdateProfilePort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UserNotFoundException;
import com.ticketflow.user.user.domain.models.UserDomain;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProfileUseCase implements UpdateProfilePort {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public GetUserResponseDTO update(UUID userId, UpdateProfileRequestDTO request) {
        UserDomain user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateProfile(request.name(), request.phone());
        userRepository.save(user);
        return GetUserResponseDTO.fromDomain(user);
    }
}
