package antifraud.repository;

import antifraud.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByNumberAndDateBetween(String number, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findAllByNumber(String number);
    List<Transaction> findAll();
    Optional<Transaction> findFirstByNumber(String number);
}
