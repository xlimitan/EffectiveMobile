package com.example.service;

import com.example.dto.CreateUserRequestDto;
import com.example.dto.UpdateUserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.entity.RoleType;
import com.example.entity.User;
import com.example.exception.RoleNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    public Page<User> findAll(int pageNumber, int size){
        Pageable pageable = PageRequest.of(pageNumber, size);
        return userRepository.findAll(pageable);
    }

    public User findById(UUID id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));
    }

    @Transactional
    public UserResponseDto save(CreateUserRequestDto requestDto) throws RoleNotFoundException {
        userValidator.validateUserCreation(requestDto);

        User user = userMapper.toUserFromCreateRequest(requestDto, passwordEncoder);
        user.addRole(roleRepository.findByRoleType(RoleType.USER).orElseThrow(() -> new RoleNotFoundException("Роль пользователя не найдена")));
        user = userRepository.saveAndFlush(user);

        return userMapper.toDtoFromUser(user);
    }

    @Transactional
    public UserResponseDto save(UpdateUserRequestDto requestDto, UUID id) throws UserNotFoundException {
        userValidator.validateUserUpdate(id, requestDto);

        User user = userMapper.toUserFromUpdateRequest(requestDto, passwordEncoder);
        User existsUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));
        user.setId(id);
        user.setCreatedAt(existsUser.getCreatedAt());
        user.setRoles(existsUser.getRoles());
        user.setCards(existsUser.getCards());
        user = userRepository.save(user);

        return userMapper.toDtoFromUser(user);
    }

    @Transactional
    public void deleteById(UUID id) throws UserNotFoundException {
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));
        userRepository.deleteById(id);
    }
}
