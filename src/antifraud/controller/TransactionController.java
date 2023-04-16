package antifraud.controller;

import antifraud.exception.CannotParseException;
import antifraud.exception.NegativeNumberException;
import antifraud.model.Transaction;
import antifraud.model.dto.TransactionFeedback;
import antifraud.model.dto.TransactionInfo;
import antifraud.model.dto.TransactionResponse;
import antifraud.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @PutMapping("/transaction")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<TransactionInfo> transactionFeedback(@RequestBody TransactionFeedback feedback) {
        TransactionInfo transactionInfo = transactionService.feedbackInfo(feedback);
        return ResponseEntity.status(200).body(transactionInfo);
    }

    @GetMapping("/history/{number}")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<List<TransactionInfo>> transactionsByCardNumber(@PathVariable String number) {
        List<TransactionInfo> transactionListByCardNumber = transactionService.getTransactions(number);
        return ResponseEntity.status(200).body(transactionListByCardNumber);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<List<TransactionInfo>> transactionsHistory() {
        List<TransactionInfo> transactionListHistory = transactionService.getTransactionsHistory();
        return ResponseEntity.status(200).body(transactionListHistory);
    }
}
