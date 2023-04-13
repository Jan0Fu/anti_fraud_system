package antifraud.controller;

import antifraud.exception.IncorrectIp;
import antifraud.exception.IpDuplicate;
import antifraud.exception.IpNotFound;
import antifraud.model.SuspectIp;
import antifraud.model.dto.DeleteIpResponse;
import antifraud.model.dto.IpResponse;
import antifraud.service.IpService;
import antifraud.utils.IpValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/antifraud/")
@PreAuthorize("hasRole('SUPPORT')")
public class IpController {

    private final IpService ipService;

    @GetMapping("/suspicious-ip")
    public List<IpResponse> getAllIPs() {
        return ipService.findAllIPs();
    }

    @PostMapping("/suspicious-ip")
    public ResponseEntity<IpResponse> addSusIp(@RequestBody @Valid SuspectIp ip) throws IpDuplicate {
        IpResponse ipResponse = ipService.saveIp(ip);
        return ResponseEntity.status(200).body(ipResponse);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<DeleteIpResponse> deleteIp(@PathVariable String ip) throws IpNotFound, IncorrectIp {
        IpValidator.validateIp(ip);
        DeleteIpResponse deleteIp = ipService.deleteIp(ip);
        return ResponseEntity.status(200).body(deleteIp);
    }
}
