package com.novamart.modules.users.mapper;

import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);
    UserResponse toResponse(User user);
}
