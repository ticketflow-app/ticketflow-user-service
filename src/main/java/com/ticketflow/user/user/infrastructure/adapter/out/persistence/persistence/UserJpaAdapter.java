package com.ticketflow.user.user.infrastructure.adapter.out.persistence.persistence;

import com.ticketflow.user.user.application.ports.out.UserRepositoryPort;
import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.mapper.UserEntityMapper;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public Optional<UserDomain> findById(UUID id) {
        return userRepository.findById(id)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<UserDomain> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
                .map(userEntityMapper::toDomain);
    }

    @Override
    public UserDomain save(UserDomain user) {
        UserEntity entity = userEntityMapper.toEntity(user);
        return userEntityMapper.toDomain(userRepository.save(entity));
    }

    @Override
    public boolean existsByEmail(String email) {
        return email != null && userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return email != null && userRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public List<UserDomain> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (query == null || query.isBlank()) {
            return userRepository.findAll(pageable).stream()
                    .map(userEntityMapper::toDomain)
                    .toList();
        }
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query, query, pageable)
                .stream()
                .map(userEntityMapper::toDomain)
                .toList();
    }

    @Override
    public long count(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.count();
        }
        return userRepository.countByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
}
