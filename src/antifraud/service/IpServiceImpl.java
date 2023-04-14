package antifraud.service;

import antifraud.exception.IpDuplicateException;
import antifraud.exception.IpNotFoundException;
import antifraud.model.SuspectIp;
import antifraud.model.dto.DeleteIpResponse;
import antifraud.model.dto.IpResponse;
import antifraud.repository.IpRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
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
    public IpResponse saveIp(SuspectIp ip) throws IpDuplicateException {
        Optional<SuspectIp> optionalIp = ipRepository.findIpsByIp(ip.getIp());
        if (optionalIp.isEmpty()) {
            ipRepository.save(ip);
        } else {
            throw new IpDuplicateException("IP already persists");
        }
        Long id = ipRepository.findById(ip.getId()).get().getId();
        return new IpResponse(id, ip.getIp());
    }

    @Override
    @Transactional
    public DeleteIpResponse deleteIp(String ip) throws IpNotFoundException {
        Optional<SuspectIp> optionalIP = ipRepository.findIpsByIp(ip);
        if (optionalIP.isEmpty()) throw new IpNotFoundException("IP not found");
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
