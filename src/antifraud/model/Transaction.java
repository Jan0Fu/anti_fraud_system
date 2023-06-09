package antifraud.model;

import javax.persistence.*;

import antifraud.constants.Region;
import antifraud.constants.TransactionOutput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@Validated
@Entity
@Table(name = "transactions", indexes = {@Index(name = "transaction_number", columnList = "number, date")})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private long id;
    private String amount;
    @NotBlank
    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$", message = "Not valid IP")
    private String ip;
    @LuhnCheck
    @NotNull
    private String number;
    @Enumerated(EnumType.STRING)
    @NotNull
    private Region region;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private TransactionOutput result;
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private TransactionOutput feedback;

    public String getFeedback() {
        return feedback == null ? "" : feedback.name();
    }
}
