package com.example.repository;

import com.example.entity.Card;
import com.example.entity.CardStatus;
import com.example.entity.User;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    Page<Card> findAll(@Nullable Pageable pageable);
    Optional<Card> findByCardNumber(String number);
    Page<Card> findAllByStatus(CardStatus cardStatus, Pageable pageable);
    Page<Card> findByOwner(User user, Pageable pageable);
}
