package com.example.dto;

import com.example.entity.CardStatus;
import com.example.entity.Transaction;
import com.example.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDto {
    private UUID id;
    private String cardNumber;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private User owner;
    private List<Transaction> outgoingTransactions = new ArrayList<>();
    private List<Transaction> incomingTransactions = new ArrayList<>();
}
