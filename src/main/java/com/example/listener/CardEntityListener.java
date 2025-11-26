package com.example.listener;

import com.example.entity.Card;
import com.example.entity.CardStatus;
import jakarta.persistence.PostLoad;

import java.time.LocalDate;

public class CardEntityListener {
    @PostLoad
    public void updateCardStatus(Card card) {
        if (card.getStatus() == CardStatus.ACTIVE && card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
        }
    }
}
