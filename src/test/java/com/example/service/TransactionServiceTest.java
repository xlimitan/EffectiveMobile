package com.example.service;

import com.example.dto.TransferRequestDto;
import com.example.entity.Card;
import com.example.entity.CardStatus;
import com.example.entity.Transaction;
import com.example.entity.TransactionStatus;
import com.example.exception.CardNotFoundException;
import com.example.exception.TransactionTransferException;
import com.example.repository.CardRepository;
import com.example.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void transferMoneyBetweenCards_success() throws CardNotFoundException {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);
        when(request.getToCardId()).thenReturn(toId);
        when(request.getAmount()).thenReturn(new BigDecimal("25.00"));

        var owner = new com.example.entity.User();
        owner.setUsername("alice");

        Card fromCard = new Card();
        fromCard.setId(fromId);
        fromCard.setOwner(owner);
        fromCard.setBalance(new BigDecimal("200.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        Card toCard = new Card();
        toCard.setId(toId);
        toCard.setOwner(owner);
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = transactionService.transferMoneyBetweenCards(request, "alice");

        assertNotNull(tx);
        assertEquals(TransactionStatus.COMPLETED, tx.getStatus());
        assertEquals(new BigDecimal("175.00"), tx.getFromCard().getBalance());
        assertEquals(new BigDecimal("75.00"), tx.getToCard().getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferMoneyBetweenCards_authorMismatch_throws() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);
        when(request.getToCardId()).thenReturn(toId);

        var ownerFrom = new com.example.entity.User();
        ownerFrom.setUsername("owner1");

        var ownerTo = new com.example.entity.User();
        ownerTo.setUsername("owner1");

        Card fromCard = new Card();
        fromCard.setId(fromId);
        fromCard.setOwner(ownerFrom);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        Card toCard = new Card();
        toCard.setId(toId);
        toCard.setOwner(ownerTo);
        toCard.setBalance(new BigDecimal("100.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));

        assertThrows(TransactionTransferException.class, () ->
                transactionService.transferMoneyBetweenCards(request, "differentUser")
        );
        verify(transactionRepository, never()).save(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transferMoneyBetweenCards_differentOwners_throws() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);
        when(request.getToCardId()).thenReturn(toId);

        var ownerFrom = new com.example.entity.User();
        ownerFrom.setUsername("owner1");
        ownerFrom.setId(UUID.randomUUID()); // явно разные id
        var ownerTo = new com.example.entity.User();
        ownerTo.setUsername("owner2");
        ownerTo.setId(UUID.randomUUID()); // явно разные id

        Card fromCard = new Card();
        fromCard.setId(fromId);
        fromCard.setOwner(ownerFrom);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        Card toCard = new Card();
        toCard.setId(toId);
        toCard.setOwner(ownerTo);
        toCard.setBalance(new BigDecimal("100.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));

        assertThrows(TransactionTransferException.class, () ->
                transactionService.transferMoneyBetweenCards(request, "owner1")
        );

        verify(transactionRepository, never()).save(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transferMoneyBetweenCards_inactiveCards_throws() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);
        when(request.getToCardId()).thenReturn(toId);

        var owner = new com.example.entity.User();
        owner.setUsername("owner1");

        Card fromCard = new Card();
        fromCard.setId(fromId);
        fromCard.setOwner(owner);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.BLOCKED); // неактивная карта

        Card toCard = new Card();
        toCard.setId(toId);
        toCard.setOwner(owner);
        toCard.setBalance(new BigDecimal("100.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));

        assertThrows(TransactionTransferException.class, () ->
                transactionService.transferMoneyBetweenCards(request, "owner1")
        );

        verify(transactionRepository, never()).save(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transferMoneyBetweenCards_insufficientFunds_throws() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);
        when(request.getToCardId()).thenReturn(toId);
        when(request.getAmount()).thenReturn(new BigDecimal("150.00"));

        var owner = new com.example.entity.User();
        owner.setUsername("owner1");

        Card fromCard = new Card();
        fromCard.setId(fromId);
        fromCard.setOwner(owner);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        Card toCard = new Card();
        toCard.setId(toId);
        toCard.setOwner(owner);
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));

        assertThrows(TransactionTransferException.class, () ->
                transactionService.transferMoneyBetweenCards(request, "owner1")
        );

        verify(transactionRepository, never()).save(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transferMoneyBetweenCards_fromCardNotFound_throws() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);

        when(cardRepository.findById(fromId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                transactionService.transferMoneyBetweenCards(request, "any")
        );
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferMoneyBetweenCards_toCardNotFound_throws() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);
        when(request.getToCardId()).thenReturn(toId);

        var owner = new com.example.entity.User();
        owner.setUsername("owner1");

        Card fromCard = new Card();
        fromCard.setId(fromId);
        fromCard.setOwner(owner);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                transactionService.transferMoneyBetweenCards(request, "owner1")
        );
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferMoneyBetweenCards_onSaveException_transactionCanceledAndSaved() throws CardNotFoundException {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequestDto request = mock(TransferRequestDto.class);
        when(request.getFromCardId()).thenReturn(fromId);
        when(request.getToCardId()).thenReturn(toId);
        when(request.getAmount()).thenReturn(new BigDecimal("25.00"));

        var owner = new com.example.entity.User();
        owner.setUsername("owner1");

        Card fromCard = new Card();
        fromCard.setId(fromId);
        fromCard.setOwner(owner);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));

        Card toCard = new Card();
        toCard.setId(toId);
        toCard.setOwner(owner);
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));

        when(cardRepository.save(fromCard)).thenThrow(new RuntimeException("db error"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.transferMoneyBetweenCards(request, "owner1");

        assertNotNull(result);
        assertEquals(TransactionStatus.CANCELED, result.getStatus());
        verify(cardRepository).save(fromCard);
        verify(transactionRepository).save(any(Transaction.class));
    }
}