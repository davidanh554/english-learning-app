package com.example.luyentap.config;

import com.example.luyentap.entity.User;
import com.example.luyentap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeder tự động tạo tài khoản Quản trị viên (ADMIN) an toàn khi ứng dụng khởi chạy lần đầu
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản Trị Viên");
            admin.setRole("ADMIN");
            userRepository.save(admin);

            log.info("==================================================================");
            log.info(" [SECURITY SEEDER] ĐÃ KHỞI TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH:");
            log.info(" Email:    {}", adminEmail);
            log.info(" Mật khẩu: admin123");
            log.info(" Quyền:    ADMIN");
            log.info("==================================================================");
        }
    }
}
