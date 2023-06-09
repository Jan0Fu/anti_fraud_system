package antifraud.controller;

import antifraud.model.SuspectIp;
import antifraud.model.dto.ActivateRequest;
import antifraud.model.dto.UpdateRoleRequest;
import antifraud.model.dto.UserRegisterRequest;
import antifraud.model.dto.UserRegisterResponse;
import antifraud.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPPORT')")
    public List<UserRegisterResponse> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/user")
    public ResponseEntity<UserRegisterResponse> addUser(@RequestBody @Valid UserRegisterRequest user) {
        return userService.register(user);
    }

    @DeleteMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Object> deleteUser(@PathVariable String username) {
        return userService.deleteUser(username);
    }

    @PutMapping("/role")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Object> updateRole(@RequestBody @Valid UpdateRoleRequest roleRequest) {
        return userService.updateRole(roleRequest);
    }

    @PutMapping("/access")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Object> activateUser(@RequestBody @Valid ActivateRequest activateReq) {
        return userService.activateUser(activateReq);
    }
}
