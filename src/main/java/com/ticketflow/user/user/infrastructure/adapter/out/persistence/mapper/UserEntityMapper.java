package com.ticketflow.user.user.infrastructure.adapter.out.persistence.mapper;

import com.ticketflow.user.user.domain.models.UserDomain;
import com.ticketflow.user.user.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserEntity toEntity(UserDomain domain);

    default UserDomain toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserDomain.from(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}