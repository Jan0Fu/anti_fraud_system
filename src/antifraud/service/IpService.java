package antifraud.service;

import antifraud.exception.IpDuplicateException;
import antifraud.exception.IpNotFoundException;
import antifraud.model.SuspectIp;
import antifraud.model.dto.DeleteIpResponse;
import antifraud.model.dto.IpResponse;

import java.util.List;

public interface IpService {

    List<IpResponse> findAllIPs();

    IpResponse saveIp(SuspectIp ip) throws IpDuplicateException;

    DeleteIpResponse deleteIp(String ip) throws IpNotFoundException;
}
