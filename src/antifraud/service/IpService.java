package antifraud.service;

import antifraud.exception.IpDuplicate;
import antifraud.exception.IpNotFound;
import antifraud.model.SuspectIp;
import antifraud.model.dto.DeleteIpResponse;
import antifraud.model.dto.IpResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IpService {

    List<IpResponse> findAllIPs();

    IpResponse saveIp(SuspectIp ip) throws IpDuplicate;

    DeleteIpResponse deleteIp(String ip) throws IpNotFound;
}
