package antifraud.controller;

import antifraud.model.Card;
import antifraud.model.dto.CardResponse;
import antifraud.model.dto.DeleteCardResponse;
import antifraud.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/antifraud")
@PreAuthorize("hasRole('SUPPORT')")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/stolencard")
    public List<CardResponse> getAllIPs() {
        return cardService.findAllCards();
    }

    @PostMapping("/stolencard")
    public ResponseEntity<CardResponse> addCard(@RequestBody @Valid Card card) {
        CardResponse cardResponse = cardService.saveCard(card);
        return ResponseEntity.status(200).body(cardResponse);
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<DeleteCardResponse> deleteCard(@PathVariable String number) {
        cardService.deleteCard(number);
        return ResponseEntity.status(200).body(
                new DeleteCardResponse(String.format("Card %s successfully removed!", number)));
    }
}
