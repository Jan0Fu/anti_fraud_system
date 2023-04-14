package antifraud.service;

import antifraud.model.Card;
import antifraud.model.dto.CardResponse;

import java.util.List;

public interface CardService {
    CardResponse saveCard(Card card);

    List<CardResponse> findAllCards();

    void deleteCard(String number);
}
