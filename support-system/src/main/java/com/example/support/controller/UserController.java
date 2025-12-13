package com.example.support.controller;

import com.example.support.entity.User;
import com.example.support.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            // Кодируем пароль перед сохранением
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            // Устанавливаем роль по умолчанию если не указана
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER");
            }

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (userDetails.getName() != null) {
                        existingUser.setName(userDetails.getName());
                    }
                    if (userDetails.getEmail() != null) {
                        existingUser.setEmail(userDetails.getEmail());
                    }
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    if (userDetails.getRole() != null) {
                        existingUser.setRole(userDetails.getRole());
                    }
                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("Пользователь успешно удален");
        }
        return ResponseEntity.notFound().build();
    }
}