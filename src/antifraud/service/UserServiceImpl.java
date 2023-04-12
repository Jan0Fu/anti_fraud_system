package antifraud.service;

import antifraud.constants.UserRole;
import antifraud.model.User;
import antifraud.model.dto.*;
import antifraud.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.module.ResolutionException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final ModelMapper mapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder encoder, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<UserRegisterResponse> register(UserRegisterRequest user) {
        Optional<User> userByUsername = userRepository.findByUsername(user.getUsername());
        if (userByUsername.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        User userToSave = userRepository.count() == 0 ?
                User.builder()
                        .name(user.getName())
                        .username(user.getUsername())
                        .password(encoder.encode(user.getPassword()))
                        .role(UserRole.ADMINISTRATOR)
                        .isAccountNonLocked(true)
                        .build() :
                User.builder()
                        .name(user.getName())
                        .username(user.getUsername())
                        .password(encoder.encode(user.getPassword()))
                        .role(UserRole.MERCHANT)
                        .isAccountNonLocked(false)
                        .build();

        userRepository.save(userToSave);
        Optional<User> byUsername = userRepository.findByUsername(userToSave.getUsername());
        Long id = byUsername.get().getId();
        return new ResponseEntity<>(mapper.map(userToSave, UserRegisterResponse.class), HttpStatus.CREATED);
    }

    @Override
    public List<UserRegisterResponse> getAllUsers() {
        return userRepository.findAll(Sort.sort(User.class).by(User::getId).ascending())
                .stream()
                .map(user -> mapper.map(user, UserRegisterResponse.class)).toList();
    }

    @Override
    @Transactional
    public ResponseEntity<Object> deleteUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userRepository.deleteByUsernameIgnoreCase(username);
        return new ResponseEntity<>(Map.of("username", username, "status", "Deleted successfully!"), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> updateRole(UpdateRoleRequest roleRequest) {
        if (!roleRequest.getRole().equals("SUPPORT") && (!roleRequest.getRole().equals("MERCHANT"))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userRepository.findByUsername(roleRequest.getUsername());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User theUser = user.get();
        if (theUser.getRole().name().equals(roleRequest.getRole())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        theUser.setRole(UserRole.valueOf(roleRequest.getRole()));
        UserRegisterResponse userDto = mapper.map(theUser, UserRegisterResponse.class);
        userRepository.save(theUser);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> activateUser(ActivateRequest activateReq) {
        Optional<User> user = userRepository.findByUsername(activateReq.getUsername());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User theUser = user.get();
        if (theUser.getRole().name().equals("ADMINISTRATOR")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String access;
        if (activateReq.getOperation().equals("UNLOCK")) {
            theUser.setAccountNonLocked(true);
            access = "unlocked";
        } else {
            theUser.setAccountNonLocked(false);
            access = "locked";
        }
        userRepository.save(theUser);
        return new ResponseEntity<>(new AccessResponse(String.format("User %s %s!", theUser.getUsername(), access)), HttpStatus.OK);
    }
}
