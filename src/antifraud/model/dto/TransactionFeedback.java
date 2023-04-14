package antifraud.model.dto;

import antifraud.constants.TransactionOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFeedback {
    Long transactionId;
    TransactionOutput feedback;
}
