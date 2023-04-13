package antifraud.service;

import antifraud.model.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TransactionService {
    public ResponseEntity<Object> validateTransaction(Transaction transaction) {
        if (transaction.getAmount() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (transaction.getAmount() <= 200) {
            return new ResponseEntity<>(Map.of("result", "ALLOWED"), HttpStatus.OK);
        } else if (transaction.getAmount() <= 1500) {
            return new ResponseEntity<>(Map.of("result", "MANUAL_PROCESSING"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("result", "PROHIBITED"), HttpStatus.OK);
        }
    }

    private String addChecksum(String cardNumber) {
        cardNumber += "0";
        int oddSum = 0;
        int evenSum = 0;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int currentNum = Character.getNumericValue(cardNumber.charAt(i));
            if (i % 2 != 0) {
                oddSum += currentNum;
            } else {
                if (currentNum * 2 > 9) {
                    evenSum += currentNum * 2 - 9;
                } else {
                    evenSum += currentNum * 2;
                }
            }
        }
        int controlSum = oddSum + evenSum;
        return String.valueOf(controlSum % 10 == 0 ? 0 : 10 - controlSum % 10);
    }
}
