package com.example.controller;

import com.example.dto.CardResponseDto;
import com.example.dto.CreateCardRequestDto;
import com.example.dto.TopUpRequestDto;
import com.example.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getAll(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(cardService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getById(@PathVariable("id") UUID id){
        return ResponseEntity.ok(cardService.findById(id));
    }

    @GetMapping("/user")
    public ResponseEntity<Page<CardResponseDto>> getByOwnerId(@RequestParam UUID ownerId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(cardService.findByOwnerId(ownerId, page, size));

    }

    @PostMapping
    public ResponseEntity<CardResponseDto> create(@Valid @RequestBody CreateCardRequestDto requestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.save(requestDto));
    }

    @PatchMapping("block/{id}")
    public ResponseEntity<CardResponseDto> block(@PathVariable("id") UUID id){
        return ResponseEntity.ok(cardService.blockCard(id));
    }

    @PatchMapping("/{id}/top-up")
    public ResponseEntity<CardResponseDto> topUpBalance(@PathVariable("id") UUID id,
                                                        @Valid @RequestBody TopUpRequestDto requestDto,
                                                        Principal principal){
        return ResponseEntity.ok(cardService.topUpCardBalance(id, requestDto, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        cardService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
