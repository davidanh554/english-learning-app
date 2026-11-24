package com.example.luyentap.service;

import com.example.luyentap.dto.LoginRequestDTO;
import com.example.luyentap.dto.RegisterRequestDTO;
import com.example.luyentap.dto.UserDTO;
import com.example.luyentap.entity.User;
import com.example.luyentap.repository.UserRepository;
import com.example.luyentap.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Tài khoản hoặc mật khẩu không chính xác"));

        // Sử dụng PasswordEncoder để kiểm tra (hỗ trợ cả chuỗi cũ và BCrypt)
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác");
        }

        String role = user.getRole() != null ? user.getRole() : "STUDENT";
        String token = jwtTokenProvider.generateToken(user.getEmail(), role, user.getFullName());

        return new UserDTO(user.getId(), user.getEmail(), user.getFullName(), role, token);
    }

    public UserDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        // Mã hóa mật khẩu mới bằng PasswordEncoder (BCrypt)
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // BẢO MẬT HỆ THỐNG: Mọi tài khoản đăng ký từ bên ngoài mặc định luôn là STUDENT!
        // Ngăn chặn lỗ hổng Privilege Escalation (Leo thang đặc quyền)
        String role = "STUDENT";
        user.setRole(role);

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getEmail(), role, saved.getFullName());

        return new UserDTO(saved.getId(), saved.getEmail(), saved.getFullName(), role, token);
    }
}