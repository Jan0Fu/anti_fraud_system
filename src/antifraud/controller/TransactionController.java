package antifraud.controller;

import antifraud.exception.CannotParseException;
import antifraud.exception.NegativeNumberException;
import antifraud.model.Transaction;
import antifraud.model.dto.TransactionFeedback;
import antifraud.model.dto.TransactionResponse;
import antifraud.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction")
    @PreAuthorize("hasRole('MERCHANT')")
    public TransactionResponse validateTransaction(@RequestBody @Valid Transaction transaction) throws CannotParseException, NegativeNumberException {
        return transactionService.validateTransaction(transaction);
    }
}
