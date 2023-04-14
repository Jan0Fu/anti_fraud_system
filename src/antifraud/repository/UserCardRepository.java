package antifraud.repository;

import antifraud.model.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    Optional<UserCard> findLastByNumber(String number);
}
