package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.LoginRequest;
import com.luciano.music_graph.dto.RegisterRequest;
import com.luciano.music_graph.exception.EmailNotFoundException;
import com.luciano.music_graph.exception.UserAlreadyExistsException;
import com.luciano.music_graph.exception.UsernameAlreadyExistsException;
import com.luciano.music_graph.mapper.UserMapper;
import com.luciano.music_graph.model.AuthProvider;
import com.luciano.music_graph.model.Role;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public User create(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) throw new UserAlreadyExistsException(request.email());
        if (userRepository.existsByUsername(request.username())) throw new UsernameAlreadyExistsException(request.username());

        User user = mapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.LOCAL);

        return userRepository.save(user);
    }

    public User access(LoginRequest request){

        return userRepository.findByEmail(request.email()).orElseThrow(()-> new EmailNotFoundException(request.email()));
    }

    public User getUserEntityByEmail(String email){

        return userRepository.findByEmail(email).orElseThrow(() -> new EmailNotFoundException(email));
    }

    private String generateUniqueUsername(String base) {
        String username = base.replaceAll("\\s+", "").toLowerCase();
        int counter = 0;

        while (userRepository.findByUsername(username).isPresent()) {
            counter++;
            username = base + counter;
        }

        return username;
    }

    public User findByIdOrCreate(String email, String name, String providerStr){

        AuthProvider provider = AuthProvider.valueOf(providerStr.toUpperCase());

        Optional<User> existing = userRepository.findByEmail(email);

        if(existing.isPresent()) {
            User user = existing.get();

            if(user.getProvider() != provider) {
                throw new RuntimeException("Email already registered with different provider");
            }

            return user;
        }

        User u = new User();
        u.setEmail(email);
        u.setUsername(generateUniqueUsername(name));
        u.setProvider(provider);
        u.setRole(Role.USER);

        return userRepository.save(u);
    }
}
