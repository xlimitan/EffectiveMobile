package com.example.service;

import com.example.dto.CreateUserRequestDto;
import com.example.dto.UpdateUserRequestDto;
import com.example.dto.UserResponseDto;
import com.example.entity.Role;
import com.example.entity.RoleType;
import com.example.entity.User;
import com.example.exception.RoleNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private com.example.validation.UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll_returnsPage() {
        User u = new User();
        u.setUsername("u1");
        Page<User> page = new PageImpl<>(List.of(u));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.findAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void findById_found() throws UserNotFoundException {
        UUID id = UUID.randomUUID();
        User u = new User();
        u.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(u));

        User result = userService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void findById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(id));
    }

    @Test
    void save_create_success() throws RoleNotFoundException {
        CreateUserRequestDto req = new CreateUserRequestDto();
        User toSave = new User();
        toSave.setUsername("new");
        User saved = new User();
        saved.setId(UUID.randomUUID());
        saved.setUsername("new");

        UserResponseDto dto = new UserResponseDto();
        dto.setUsername("new");

        when(userMapper.toUserFromCreateRequest(req, passwordEncoder)).thenReturn(toSave);
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.of(new Role()));
        when(userRepository.saveAndFlush(toSave)).thenReturn(saved);
        when(userMapper.toDtoFromUser(saved)).thenReturn(dto);

        UserResponseDto result = userService.save(req);

        assertNotNull(result);
        assertEquals("new", result.getUsername());
        verify(userValidator).validateUserCreation(req);
        verify(roleRepository).findByRoleType(RoleType.USER);
        verify(userRepository).saveAndFlush(toSave);
        verify(userMapper).toDtoFromUser(saved);
    }

    @Test
    void save_create_roleNotFound_throws() {
        CreateUserRequestDto req = new CreateUserRequestDto();
        when(userMapper.toUserFromCreateRequest(req, passwordEncoder)).thenReturn(new User());
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> userService.save(req));
        verify(userValidator).validateUserCreation(req);
        verify(roleRepository).findByRoleType(RoleType.USER);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void save_update_success() throws UserNotFoundException {
        UUID id = UUID.randomUUID();
        UpdateUserRequestDto req = new UpdateUserRequestDto();
        User mapped = new User();
        mapped.setUsername("upd");
        User existing = new User();
        existing.setId(id);
        existing.setUsername("old");
        existing.setCreatedAt(existing.getCreatedAt()); // keep createdAt
        User saved = new User();
        saved.setId(id);
        saved.setUsername("upd");

        UserResponseDto dto = new UserResponseDto();
        dto.setUsername("upd");

        when(userMapper.toUserFromUpdateRequest(req, passwordEncoder)).thenReturn(mapped);
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userMapper.toDtoFromUser(saved)).thenReturn(dto);

        UserResponseDto result = userService.save(req, id);

        assertNotNull(result);
        assertEquals("upd", result.getUsername());
        verify(userValidator).validateUserUpdate(id, req);
        verify(userRepository).findById(id);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void save_update_userNotFound_throws() {
        UUID id = UUID.randomUUID();
        UpdateUserRequestDto req = new UpdateUserRequestDto();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(userMapper.toUserFromUpdateRequest(req, passwordEncoder)).thenReturn(new User());

        assertThrows(UserNotFoundException.class, () -> userService.save(req, id));
        verify(userValidator).validateUserUpdate(id, req);
        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteById_success() throws UserNotFoundException {
        UUID id = UUID.randomUUID();
        User u = new User();
        u.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(u));

        userService.deleteById(id);

        verify(userRepository).findById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteById(id));
        verify(userRepository).findById(id);
        verify(userRepository, never()).deleteById(any());
    }
}