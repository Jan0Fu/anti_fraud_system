package antifraud.service;

import antifraud.model.dto.UserRegisterRequest;
import antifraud.model.dto.UserRegisterResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface UserService {

    ResponseEntity<UserRegisterResponse> register(UserRegisterRequest user);
    List<UserRegisterResponse> getAllUsers();

    ResponseEntity<Object> deleteUser(String username);
}
