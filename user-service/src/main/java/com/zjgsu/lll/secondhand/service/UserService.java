package com.zjgsu.lll.secondhand.service;

import com.zjgsu.lll.secondhand.entity.User;
import com.zjgsu.lll.secondhand.exception.BusinessException;
import com.zjgsu.lll.secondhand.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        // ✅ 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        if (!existingUser.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        if (!existingUser.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setAddress(user.getAddress());

        // ✅ 如果修改密码，加密后保存
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("User not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * ✅ 用户认证（登录验证）- 使用加密密码比对
     */
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return null;
        }

        // ✅ 使用 BCrypt 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        if (user.getStatus() == 0) {
            return null; // 账户被禁用
        }

        return user;
    }

    /**
     * ✅ 保存用户（用于注册）- 加密密码
     */
    @Transactional
    public User save(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // ✅ 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1); // 默认启用

        return userRepository.save(user);
    }
}