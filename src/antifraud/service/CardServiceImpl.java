package antifraud.service;

import antifraud.model.Card;
import antifraud.model.dto.CardResponse;
import antifraud.repository.CardRepository;
import antifraud.utils.IpValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ModelMapper mapper;

    @Override
    public CardResponse saveCard(Card card) {
        Optional<Card> optionalCard = cardRepository.findByNumber(card.getNumber());
        if (optionalCard.isEmpty()) {
            cardRepository.save(card);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        return new CardResponse(card.getId(), card.getNumber());
    }

    @Override
    public List<CardResponse> findAllCards() {
        return cardRepository.findAll(Sort.sort(Card.class).by(Card::getId).ascending())
                .stream()
                .map(card -> mapper.map(card, CardResponse.class)).toList();
    }

    @Override
    @Transactional
    public void deleteCard(String number) {
        IpValidator.validateNumber(number);
        Optional<Card> optionalCard = cardRepository.findByNumber(number);
        if (optionalCard.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            cardRepository.deleteById(optionalCard.get().getId());
        }
    }
}
