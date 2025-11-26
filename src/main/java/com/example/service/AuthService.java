package com.example.service;

import com.example.dto.AuthRequestDto;
import com.example.dto.AuthResponseDto;
import com.example.dto.CreateUserRequestDto;
import com.example.entity.RoleType;
import com.example.entity.User;
import com.example.exception.RoleNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDto login(AuthRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким логином не найден"));
        
        AuthResponseDto response = new AuthResponseDto();
        response.setToken(jwt);
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles());

        return response;
    }

    public AuthResponseDto register(CreateUserRequestDto request) throws Throwable {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким логином уже зарегистрирован");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSurname(request.getSurname());
        user.setName(request.getName());
        user.setPatronymic(request.getPatronymic());
        user.setBirthdayYear(request.getBirthdayYear());
        user.addRole(roleRepository.findByRoleType(RoleType.USER).orElseThrow((Supplier<Throwable>) () -> new RoleNotFoundException("Роль пользователя не найдена в БД")));

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

        AuthResponseDto response = new AuthResponseDto();
        response.setToken(jwt);
        response.setUsername(savedUser.getUsername());
        response.setRoles(savedUser.getRoles());

        return response;
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким логином не существует"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
