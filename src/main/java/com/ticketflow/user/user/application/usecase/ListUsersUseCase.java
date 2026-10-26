package com.ticketflow.user.user.application.usecase;

import com.ticketflow.user.common.application.dto.PageResponse;
import com.ticketflow.user.user.application.ports.in.dto.GetUserResponseDTO;
import com.ticketflow.user.user.application.ports.in.service.ListUsersPort;
import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.exception.business.UnauthorizedAdminActionException;
import com.ticketflow.user.user.domain.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase implements ListUsersPort {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepositoryPort userRepository;

    @Override
    public PageResponse<GetUserResponseDTO> execute(UUID actorUserId, UserRole actorRole, String query,
                                                     int page, int size) {
        if (actorRole != UserRole.ADMIN) {
            throw new UnauthorizedAdminActionException();
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String safeQuery = query == null ? "" : query.trim();

        List<GetUserResponseDTO> content = userRepository.search(safeQuery, safePage, safeSize)
                .stream()
                .map(GetUserResponseDTO::fromDomain)
                .toList();
        long total = userRepository.count(safeQuery);
        return PageResponse.of(content, safePage, safeSize, total);
    }
}
