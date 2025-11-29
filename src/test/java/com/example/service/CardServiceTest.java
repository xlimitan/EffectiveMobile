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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardService cardService;

    @Test
    void findAll_returnsMappedPage() {
        Card card = new Card();
        List<Card> cards = List.of(card);
        Page<Card> cardPage = new PageImpl<>(cards);
        when(cardRepository.findAll(PageRequest.of(0, 10))).thenReturn(cardPage);

        CardResponseDto dto = new CardResponseDto();
        when(cardMapper.toDtoFromCard(card)).thenReturn(dto);

        Page<CardResponseDto> result = cardService.findAll(0, 10);

        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().get(0));
    }

    @Test
    void findById_found_returnsDto() throws CardNotFoundException {
        UUID id = UUID.randomUUID();
        Card card = new Card();
        when(cardRepository.findById(id)).thenReturn(Optional.of(card));

        CardResponseDto dto = new CardResponseDto();
        when(cardMapper.toDtoFromCard(card)).thenReturn(dto);

        CardResponseDto result = cardService.findById(id);

        assertSame(dto, result);
    }

    @Test
    void findById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.findById(id));
    }

    @Test
    void findByOwnerId_found_returnsMappedPage() {
        UUID ownerId = UUID.randomUUID();
        User owner = new User();
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Card card = new Card();
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findByOwner(owner, PageRequest.of(1, 5))).thenReturn(page);

        CardResponseDto dto = new CardResponseDto();
        when(cardMapper.toDtoFromCard(card)).thenReturn(dto);

        Page<CardResponseDto> result = cardService.findByOwnerId(ownerId, 1, 5);

        assertEquals(1, result.getTotalElements());
        assertSame(dto, result.getContent().get(0));
    }

    @Test
    void findByOwnerId_userNotFound_throws() {
        UUID ownerId = UUID.randomUUID();
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.findByOwnerId(ownerId, 0, 10));
    }

    @Test
    void save_success_setsNumberAndOwnerAndReturnsDto() throws UserNotFoundException {
        UUID ownerId = UUID.randomUUID();
        CreateCardRequestDto requestDto = mock(CreateCardRequestDto.class);
        when(requestDto.getOwnerId()).thenReturn(ownerId);

        Card card = new Card();
        when(cardMapper.toCardFromCreateRequest(requestDto)).thenReturn(card);

        User owner = new User();
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        when(cardNumberGenerator.generate()).thenReturn("1234-5678");
        Card saved = new Card();
        when(cardRepository.save(card)).thenReturn(saved);

        CardResponseDto dto = new CardResponseDto();
        when(cardMapper.toDtoFromCard(saved)).thenReturn(dto);

        CardResponseDto result = cardService.save(requestDto);

        assertSame(dto, result);
        assertEquals("1234-5678", card.getCardNumber());
        assertSame(owner, card.getOwner());
        verify(cardRepository).save(card);
    }

    @Test
    void save_userNotFound_throws() {
        UUID ownerId = UUID.randomUUID();
        CreateCardRequestDto requestDto = mock(CreateCardRequestDto.class);
        when(requestDto.getOwnerId()).thenReturn(ownerId);
        Card card = new Card();
        when(cardMapper.toCardFromCreateRequest(requestDto)).thenReturn(card);

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.save(requestDto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_success_setsBlockedAndReturnsDto() throws CardNotFoundException {
        UUID id = UUID.randomUUID();
        Card card = new Card();
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(id)).thenReturn(Optional.of(card));

        Card saved = new Card();
        saved.setStatus(CardStatus.BLOCKED);
        when(cardRepository.save(card)).thenReturn(saved);

        CardResponseDto dto = new CardResponseDto();
        when(cardMapper.toDtoFromCard(saved)).thenReturn(dto);

        CardResponseDto result = cardService.blockCard(id);

        assertSame(dto, result);
        assertEquals(CardStatus.BLOCKED, saved.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(id));
    }

    @Test
    void deleteById_success_callsDelete() throws CardNotFoundException {
        UUID id = UUID.randomUUID();
        Card card = new Card();
        when(cardRepository.findById(id)).thenReturn(Optional.of(card));

        cardService.deleteById(id);

        verify(cardRepository).deleteById(id);
    }

    @Test
    void deleteById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.deleteById(id));
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    void topUpCardBalance_success_updatesBalance() throws CardNotFoundException {
        UUID cardId = UUID.randomUUID();
        User owner = new User();
        owner.setUsername("alice");

        Card card = new Card();
        card.setOwner(owner);
        card.setBalance(new BigDecimal("100.00"));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        TopUpRequestDto requestDto = mock(TopUpRequestDto.class);
        when(requestDto.getAmount()).thenReturn(new BigDecimal("25.50"));

        Card saved = new Card();
        saved.setBalance(new BigDecimal("125.50"));
        when(cardRepository.save(card)).thenReturn(saved);

        CardResponseDto dto = new CardResponseDto();
        when(cardMapper.toDtoFromCard(saved)).thenReturn(dto);

        CardResponseDto result = cardService.topUpCardBalance(cardId, requestDto, "alice");

        assertSame(dto, result);
        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());
        assertEquals(new BigDecimal("125.50"), captor.getValue().getBalance());
    }

    @Test
    void topUpCardBalance_ownerMismatch_throws() {
        UUID cardId = UUID.randomUUID();
        User owner = new User();
        owner.setUsername("owner1");

        Card card = new Card();
        card.setOwner(owner);
        card.setBalance(new BigDecimal("50.00"));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        TopUpRequestDto requestDto = mock(TopUpRequestDto.class);
        // не мокать requestDto.getAmount() — метод не должен вызываться при несовпадении владельца

        assertThrows(TransactionTransferException.class, () -> cardService.topUpCardBalance(cardId, requestDto, "otherUser"));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void topUpCardBalance_cardNotFound_throws() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());
        TopUpRequestDto requestDto = mock(TopUpRequestDto.class);

        assertThrows(CardNotFoundException.class, () -> cardService.topUpCardBalance(cardId, requestDto, "any"));
    }
}