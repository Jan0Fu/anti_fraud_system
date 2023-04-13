package antifraud.repository;

import antifraud.model.SuspectIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IpRepository extends JpaRepository<SuspectIp, Long> {
    Optional<SuspectIp> findIpsByIp(String ip);
    List<SuspectIp> findAll();
    void deleteByIp(String ip);
}
