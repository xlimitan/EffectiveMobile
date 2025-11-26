package com.example.service;

import com.example.dto.CardResponseDto;
import com.example.dto.CreateCardRequestDto;
import com.example.dto.TopUpRequestDto;
import com.example.entity.Card;
import com.example.entity.CardStatus;
import com.example.entity.User;
import com.example.exception.CardNotFoundException;
import com.example.exception.TransactionTransferException;
import com.example.exception.UserNotFoundException;
import com.example.mapper.CardMapper;
import com.example.repository.CardRepository;
import com.example.repository.UserRepository;
import com.example.util.CardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CardNumberGenerator cardNumberGenerator;

    public Page<CardResponseDto> findAll(int pageNumber, int size){
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<Card> cardPage = cardRepository.findAll(pageable);
        return cardPage.map(cardMapper::toDtoFromCard);
    }

    public CardResponseDto findById(UUID id) throws CardNotFoundException {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Карта с id = " + id + " не найдена"));
        return cardMapper.toDtoFromCard(card);
    }

    public Page<CardResponseDto> findByOwnerId(UUID ownerId, int pageNumber, int size){
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + ownerId + " не найден"));
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<Card> cards = cardRepository.findByOwner(owner, pageable);
        return cards.map(cardMapper::toDtoFromCard);
    }

    @Transactional
    public CardResponseDto save(CreateCardRequestDto requestDto) throws UserNotFoundException{
        Card card = cardMapper.toCardFromCreateRequest(requestDto);
        card.setCardNumber(cardNumberGenerator.generate());
        card.setOwner(userRepository.findById(requestDto.getOwnerId()).orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + requestDto.getOwnerId() + " не найден")));
        card = cardRepository.save(card);
        return cardMapper.toDtoFromCard(card);
    }

    @Transactional
    public CardResponseDto blockCard(UUID id) throws CardNotFoundException {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Карта с id = " + id + " не найдена"));
        card.setStatus(CardStatus.BLOCKED);
        card = cardRepository.save(card);
        return cardMapper.toDtoFromCard(card);
    }

    @Transactional
    public void deleteById(UUID id) throws CardNotFoundException {
        cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Карта с id = " + id + " не найдена"));
        cardRepository.deleteById(id);
    }

    @Transactional
    public CardResponseDto topUpCardBalance(UUID cardId, TopUpRequestDto requestDto, String username)  throws CardNotFoundException {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Карта с id = " + cardId + " не найдена"));

        if (!username.equals(card.getOwner().getUsername())){
            throw new TransactionTransferException("Пользователь может пополнять только свой баланс");
        }

        BigDecimal newBalance = card.getBalance().add(requestDto.getAmount());
        card.setBalance(newBalance);
        card = cardRepository.save(card);
        return cardMapper.toDtoFromCard(card);
    }
}
