package antifraud.service;

import antifraud.constants.AmountVerification;
import antifraud.constants.TransactionOutput;
import antifraud.exception.CannotParseException;
import antifraud.exception.NegativeNumberException;
import antifraud.model.Card;
import antifraud.model.SuspectIp;
import antifraud.model.Transaction;
import antifraud.model.UserCard;
import antifraud.model.dto.TransactionFeedback;
import antifraud.model.dto.TransactionInfo;
import antifraud.model.dto.TransactionResponse;
import antifraud.repository.CardRepository;
import antifraud.repository.IpRepository;
import antifraud.repository.TransactionRepository;
import antifraud.repository.UserCardRepository;
import antifraud.utils.IpValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                .findByNumberAndDateBetween(transaction.getNumber(), transaction.getDate().minusHours(1), transaction.getDate());

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

    @Transactional
    public TransactionInfo feedbackInfo(TransactionFeedback feedback) {
        Optional<Transaction> transactionById = transactionRepository.findById(feedback.getTransactionId());

        checkForValidFeedback(feedback, transactionById);
        Transaction transaction = transactionById.get();
        checkingCard = userCardRepository.findLastByNumber(transaction.getNumber()).get();
        setMaxAllowedValueAndMaxManualValue(feedback, transaction);
        userCardRepository.save(checkingCard);
        transaction.setFeedback(feedback.getFeedback());
        transactionRepository.save(transaction);
        return mapper.map(transaction, TransactionInfo.class);
    }

    public List<TransactionInfo> getTransactions(String number) {
        IpValidator.validateNumber(number);
        Optional<Transaction> optionalTransaction = transactionRepository.findFirstByNumber(number);
        if (optionalTransaction.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return transactionRepository.findAllByNumber(number)
                .stream().map(t -> mapper.map(t, TransactionInfo.class)).toList();
    }

    public List<TransactionInfo> getTransactionsHistory() {
        return transactionRepository.findAll()
                .stream().map(transaction -> mapper.map(transaction, TransactionInfo.class)).toList();
    }

    private static void checkForValidFeedback(TransactionFeedback feedback, Optional<Transaction> transactionById) {
        if (transactionById.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else if (!transactionById.get().getFeedback().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        } else if (transactionById.get().getResult().equals(feedback.getFeedback())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void setMaxAllowedValueAndMaxManualValue(TransactionFeedback feedback, Transaction transaction) {
        long maxAllowedValue = checkingCard.getAllowedValue();
        long maxManualValue = checkingCard.getManualValue();
        long transactionAmount = Long.parseLong(transaction.getAmount());

        double allowedLimitDecrease = Math.ceil((LIMIT_MODIFIER * maxAllowedValue) - (TRANSACTION_MODIFIER * transactionAmount));
        double allowedLimitIncrease = Math.ceil((LIMIT_MODIFIER * maxAllowedValue) + (TRANSACTION_MODIFIER * transactionAmount));
        double manualLimitDecrease = Math.ceil((LIMIT_MODIFIER * maxManualValue) - (TRANSACTION_MODIFIER * transactionAmount));
        double manualLimitIncrease = Math.ceil((LIMIT_MODIFIER * maxManualValue) + (TRANSACTION_MODIFIER * transactionAmount));

        if (transaction.getResult().equals(TransactionOutput.ALLOWED) && feedback.getFeedback().equals(TransactionOutput.MANUAL_PROCESSING)) {
            checkingCard.setAllowedValue((int) allowedLimitDecrease);
        } else if (transaction.getResult().equals(TransactionOutput.ALLOWED) && feedback.getFeedback().equals(TransactionOutput.PROHIBITED)) {
            checkingCard.setAllowedValue((int) allowedLimitDecrease);
            checkingCard.setManualValue((int) manualLimitDecrease);
        } else if (transaction.getResult().equals(TransactionOutput.MANUAL_PROCESSING) && feedback.getFeedback().equals(TransactionOutput.ALLOWED)) {
            checkingCard.setAllowedValue((int) allowedLimitIncrease);
        } else if (transaction.getResult().equals(TransactionOutput.MANUAL_PROCESSING) && feedback.getFeedback().equals(TransactionOutput.PROHIBITED)) {
            checkingCard.setManualValue((int) manualLimitDecrease);
        } else if (transaction.getResult().equals(TransactionOutput.PROHIBITED) && feedback.getFeedback().equals(TransactionOutput.ALLOWED)) {
            checkingCard.setAllowedValue((int) allowedLimitIncrease);
            checkingCard.setManualValue((int) manualLimitIncrease);
        } else if (transaction.getResult().equals(TransactionOutput.PROHIBITED) && feedback.getFeedback().equals(TransactionOutput.MANUAL_PROCESSING)) {
            checkingCard.setManualValue((int) manualLimitIncrease);
        }
    }
}
