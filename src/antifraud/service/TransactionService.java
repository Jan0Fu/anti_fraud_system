package antifraud.service;

import antifraud.constants.AmountVerification;
import antifraud.constants.TransactionOutput;
import antifraud.exception.CannotParseException;
import antifraud.exception.NegativeNumberException;
import antifraud.model.Card;
import antifraud.model.SuspectIp;
import antifraud.model.Transaction;
import antifraud.model.UserCard;
import antifraud.model.dto.TransactionResponse;
import antifraud.repository.CardRepository;
import antifraud.repository.IpRepository;
import antifraud.repository.TransactionRepository;
import antifraud.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final IpRepository ipRepository;
    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final ModelMapper mapper;
    private static final int NUMBER_OF_REQUESTS = 3;
    private static final double LIMIT_MODIFIER = 0.8;
    private static final double TRANSACTION_MODIFIER = 0.2;
    private UserCard checkingCard;

    @Transactional
    public TransactionResponse validateTransaction(Transaction transaction) throws CannotParseException, NegativeNumberException {
        if ((transaction.getAmount() == null) || transaction.getAmount().startsWith("-") || transaction.getAmount().equals("0")) {
            throw new NegativeNumberException("Amount should be positive number");
        }
        long amountNumber;
        try {
            amountNumber = Long.parseLong(transaction.getAmount());
        } catch (Exception exception) {
            throw new CannotParseException("Amount should contains only numbers");
        }
        Transaction newTransaction = transactionRepository.save(transaction);

        Optional<SuspectIp> ip = ipRepository.findIpsByIp(transaction.getIp());
        Optional<Card> card = cardRepository.findByNumber(transaction.getNumber());
        List<Transaction> listOfTransactions = transactionRepository
                .findByNumber(transaction.getNumber());

        List<String> stringResults = new ArrayList<>();
        long iPRequests = listOfTransactions.stream().map(Transaction::getIp).distinct().count();
        long regionRequests = listOfTransactions.stream().map(Transaction::getRegion).distinct().count();

        if (newTransaction.getNumber() != null && card.isEmpty()) {
            if (userCardRepository.findLastByNumber(newTransaction.getNumber()).isEmpty()) {
                checkingCard = userCardRepository.save(new UserCard(newTransaction.getNumber()));
            } else {
                checkingCard = userCardRepository.findLastByNumber(newTransaction.getNumber()).get();
            }
        }

        if (checkForProhibitedActions(iPRequests, regionRequests, stringResults, amountNumber, ip, card, checkingCard)) {
            newTransaction.setResult(TransactionOutput.PROHIBITED);
        } else if (checksForManualProcessing(iPRequests, regionRequests, stringResults, amountNumber, checkingCard)) {
            newTransaction.setResult(TransactionOutput.MANUAL_PROCESSING);
        } else {
            newTransaction.setResult(TransactionOutput.ALLOWED);
            stringResults.add("none");
        }
        transactionRepository.save(newTransaction);
        return new TransactionResponse(newTransaction.getResult(), stringResults.stream().sorted().collect(Collectors.joining(", ")));
    }

    private boolean checksForManualProcessing
            (long iPRequests, long regionRequests, List<String> result, long amountNumber, UserCard checkingCard) {
        boolean flag = false;

        if (checkingCard != null) {
            if ((amountNumber > checkingCard.getAllowedValue() && amountNumber <= checkingCard.getManualValue())) {
                result.add("amount");
                flag = true;
            }
        } else if ((amountNumber > AmountVerification.ALLOWED.getAmount() && amountNumber <= AmountVerification.MANUAL_PROCESSING.getAmount())) {
            result.add("amount");
            flag = true;
        }

        if (iPRequests == NUMBER_OF_REQUESTS && regionRequests == NUMBER_OF_REQUESTS) {
            result.add("ip-correlation");
            result.add("region-correlation");
            flag = true;
        } else if (iPRequests == NUMBER_OF_REQUESTS) {
            result.add("ip-correlation");
            flag = true;
        } else if (regionRequests == NUMBER_OF_REQUESTS) {
            result.add("region-correlation");
            flag = true;
        }
        return flag;
    }

    private boolean checkForProhibitedActions(long iPRequests, long regionRequests, List<String> result,
                                              long amountNumber, Optional<SuspectIp> ip, Optional<Card> card, UserCard checkingCard) {
        boolean flag = false;

        if (checkingCard != null) {
            if (amountNumber > checkingCard.getManualValue()) {
                result.add("amount");
                flag = true;
            }
        } else if (amountNumber > AmountVerification.MANUAL_PROCESSING.getAmount()) {
            result.add("amount");
            flag = true;
        }

        if (iPRequests > NUMBER_OF_REQUESTS && regionRequests > NUMBER_OF_REQUESTS) {
            result.add("ip-correlation");
            result.add("region-correlation");
            flag = true;
        } else if (iPRequests > NUMBER_OF_REQUESTS) {
            result.add("ip-correlation");
            flag = true;
        } else if (regionRequests > NUMBER_OF_REQUESTS) {
            result.add("region-correlation");
            flag = true;
        }
        if (ip.isPresent() && card.isPresent()) {
            result.add("card-number");
            result.add("ip");
            flag = true;
        } else if (card.isPresent()) {
            result.add("card-number");
            flag = true;
        } else if (ip.isPresent()) {
            result.add("ip");
            flag = true;
        }
        return flag;
    }
}
