package com.example.controller;

import com.example.dto.TransferRequestDto;
import com.example.entity.Transaction;
import com.example.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<Transaction>> getAll(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(transactionService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable("id") UUID id){
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @GetMapping("/user")
    public ResponseEntity<Page<Transaction>> getByOwnerId(@RequestParam UUID ownerId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(transactionService.findAllByUserId(ownerId, page, size));
    }

    @GetMapping("/card")
    public ResponseEntity<Page<Transaction>> getByCardId(@RequestParam UUID cardId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(transactionService.findAllByCardId(cardId, page, size));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> create(@Valid @RequestBody TransferRequestDto requestDto,
                                              Principal principal){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transferMoneyBetweenCards(requestDto, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
