package com.example.mapper;

import com.example.dto.CreateUserRequestDto;
import com.example.dto.UpdateUserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.entity.User;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toUserFromCreateRequest(CreateUserRequestDto requestDto, @Context PasswordEncoder passwordEncoder);
    @AfterMapping
    default void encodePassword(CreateUserRequestDto requestDto,
                                @Context PasswordEncoder passwordEncoder,
                                @MappingTarget User user) {
        if (requestDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        }
    }

    @Mapping(target = "password", ignore = true)
    User toUserFromUpdateRequest(UpdateUserRequestDto requestDto, @Context PasswordEncoder passwordEncoder);
    @AfterMapping
    default void encodePassword(UpdateUserRequestDto requestDto,
                                @Context PasswordEncoder passwordEncoder,
                                @MappingTarget User user) {
        if (requestDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        }
    }

    UserResponseDto toDtoFromUser(User user);
}
