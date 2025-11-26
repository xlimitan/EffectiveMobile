package com.example.dto;

import com.example.entity.Card;
import com.example.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String surname;
    private String name;
    private String patronymic;
    private int birthdayYear;
    private String username;
    private LocalDateTime createdAt;
    private Set<Role> roles = new HashSet<>();
    private List<Card> cards = new ArrayList<>();
}
