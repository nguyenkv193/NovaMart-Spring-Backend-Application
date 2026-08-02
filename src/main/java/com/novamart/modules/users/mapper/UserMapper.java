package com.novamart.modules.users.mapper;

import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toEntity(CreateUserRequest createUserRequest);
    UserResponse toDTO(User user);
}
