package com.attendance.service;

import com.attendance.entity.User;
import com.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        user.setStatus("PENDING");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public List<User> findPendingUsers() {
        return userRepository.findByStatus("PENDING");
    }

    @Transactional
    public void approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus("APPROVED");
        userRepository.save(user);
    }

    @Transactional
    public void rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus("REJECTED");
        userRepository.save(user);
    }

    public boolean checkPassword(String rawPassword, String storedPassword) {
        return rawPassword.equals(storedPassword);
    }
}