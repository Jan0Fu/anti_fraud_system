package antifraud.service;

import antifraud.model.User;
import antifraud.model.dto.UserRegisterRequest;
import antifraud.model.dto.UserRegisterResponse;
import antifraud.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
        User userToSave = User.builder()
                        .name(user.getName())
                        .username(user.getUsername())
                        .password(encoder.encode(user.getPassword()))
                        .isAccountNonLocked(true)
                        .build();
        userRepository.save(userToSave);
        Optional<User> byUsername = userRepository.findByUsername(userToSave.getUsername());
        Long id = byUsername.get().getId();
        return new ResponseEntity<>(new UserRegisterResponse(id, userToSave.getName(), userToSave.getUsername(), userToSave.getRole()), HttpStatus.CREATED);
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
}
