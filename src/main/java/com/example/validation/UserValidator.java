package com.example.validation;

import com.example.dto.CreateUserRequestDto;
import com.example.dto.UpdateUserRequestDto;
import com.example.entity.User;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;

    public void validateUserCreation(CreateUserRequestDto requestDto) throws UserAlreadyExistsException {
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()){
            throw new UserAlreadyExistsException("Пользователь с таким логином уже существует");
        }
    }

    public void validateUserUpdate(UUID id, UpdateUserRequestDto requestDto) throws UserNotFoundException {
        User existsUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));
        if (!existsUser.getUsername().equals(requestDto.getUsername()) && userRepository.findByUsername(requestDto.getUsername()).isPresent()){
            throw new UserAlreadyExistsException("Пользователь с таким логином уже существует");
        }
    }
}
