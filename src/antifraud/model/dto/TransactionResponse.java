package antifraud.model.dto;

import antifraud.constants.TransactionOutput;
import lombok.Value;

@Value
public class TransactionResponse {
    TransactionOutput result;
    String info;
}
