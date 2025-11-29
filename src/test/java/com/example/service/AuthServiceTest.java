package com.example.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.dto.AuthRequestDto;
import com.example.dto.CreateUserRequestDto;
import com.example.entity.Role;
import com.example.entity.RoleType;
import com.example.entity.User;
import com.example.exception.RoleNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_success() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("john");
        request.setPassword("pwd");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("token-123");

        User user = new User();
        user.setUsername("john");
        Role role = mock(Role.class);
        user.setRoles(Set.of(role));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals("token-123", response.getToken());
        assertEquals("john", response.getUsername());
        assertEquals(user.getRoles(), response.getRoles());
        // Authentication placed to SecurityContext
        assertSame(auth, SecurityContextHolder.getContext().getAuthentication());
        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(auth);
    }

    @Test
    void login_userNotFound_throws() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("no-user");
        request.setPassword("pwd");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByUsername("no-user")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void register_success() throws Throwable {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("newuser");
        request.setPassword("pass");
        request.setName("Name");
        request.setSurname("Surname");
        request.setPatronymic("Patron");
        request.setBirthdayYear(1990);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        Role role = mock(Role.class);
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.of(role));

        when(passwordEncoder.encode("pass")).thenReturn("encoded-pass");

        User saved = new User();
        saved.setUsername("newuser");
        saved.setRoles(Set.of(role));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt-token");

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("newuser", response.getUsername());
        assertEquals(saved.getRoles(), response.getRoles());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("newuser", captor.getValue().getUsername());
        assertEquals("encoded-pass", captor.getValue().getPassword());
        verify(roleRepository).findByRoleType(RoleType.USER);
        verify(passwordEncoder).encode("pass");
        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(auth);
    }

    @Test
    void register_usernameExists_throws() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("exists");
        request.setPassword("pwd");

        User existing = new User();
        existing.setUsername("exists");
        when(userRepository.findByUsername("exists")).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_roleNotFound_throws() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("nouser");
        request.setPassword("pwd");

        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_success() {
        String username = "john";
        String newPassword = "newpwd";

        User user = new User();
        user.setUsername(username);
        user.setPassword("old");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded-new");

        authService.changePassword(username, newPassword);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded-new", captor.getValue().getPassword());
    }

    @Test
    void changePassword_userNotFound_throws() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> authService.changePassword("missing", "pwd"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void userExists_true() {
        User user = new User();
        user.setUsername("u");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        assertTrue(authService.userExists("u"));
    }

    @Test
    void userExists_false() {
        when(userRepository.findByUsername("no")).thenReturn(Optional.empty());
        assertFalse(authService.userExists("no"));
    }
}