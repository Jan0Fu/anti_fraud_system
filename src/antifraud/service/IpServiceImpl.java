package antifraud.service;

import antifraud.exception.IpDuplicate;
import antifraud.exception.IpNotFound;
import antifraud.model.SuspectIp;
import antifraud.model.dto.DeleteIpResponse;
import antifraud.model.dto.IpResponse;
import antifraud.repository.IpRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class IpServiceImpl implements IpService {

    private final IpRepository ipRepository;
    private final ModelMapper mapper;

    public IpServiceImpl(IpRepository ipRepository, ModelMapper mapper) {
        this.ipRepository = ipRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public IpResponse saveIp(SuspectIp ip) throws IpDuplicate {
        Optional<SuspectIp> optionalIp = ipRepository.findIpsByIp(ip.getIp());
        if (optionalIp.isEmpty()) {
            ipRepository.save(ip);
        } else {
            throw new IpDuplicate("IP already persists");
        }
        Long id = ipRepository.findById(ip.getId()).get().getId();
        return new IpResponse(id, ip.getIp());
    }

    @Override
    @Transactional
    public DeleteIpResponse deleteIp(String ip) throws IpNotFound {
        Optional<SuspectIp> optionalIP = ipRepository.findIpsByIp(ip);
        if (optionalIP.isEmpty()) throw new IpNotFound("IP not found");
        ipRepository.deleteByIp(ip);
        return new DeleteIpResponse(String.format("IP %s successfully removed!", ip));
    }

    @Override
    public List<IpResponse> findAllIPs() {
        return ipRepository.findAll(Sort.sort(SuspectIp.class).by(SuspectIp::getId).ascending())
                .stream()
                .map(ip -> mapper.map(ip, IpResponse.class)).toList();
    }
}
