package com.example.mapper;

import com.example.dto.CardResponseDto;
import com.example.dto.CreateCardRequestDto;
import com.example.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "owner", ignore = true)
    Card toCardFromCreateRequest(CreateCardRequestDto requestDto);

    @Mapping(target = "cardNumber", expression = "java(maskCardNumber(card.getCardNumber()))")
    CardResponseDto toDtoFromCard(Card card);

    default String maskCardNumber(String number){
        String lastFourDigits = number.substring(number.length() - 4);
        return String.format("**** **** **** %s", lastFourDigits);
    }
}
