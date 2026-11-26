package com.example.luyentap.config;

import com.example.luyentap.entity.Product;
import com.example.luyentap.entity.User;
import com.example.luyentap.repository.ProductRepository;
import com.example.luyentap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeder tự động tạo tài khoản Admin và Sản phẩm mẫu (Lab 04) khi khởi chạy
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Khởi tạo tài khoản Quản trị viên mặc định
        String adminEmail = "admin@gmail.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản Trị Viên");
            admin.setRole("ADMIN");
            userRepository.save(admin);

            log.info(" [SECURITY SEEDER] Đã tạo tài khoản ADMIN: admin@gmail.com / admin123");
        }

        // 2. Khởi tạo danh mục sản phẩm mẫu cho Cửa hàng (Lab 04 - Shopping Cart)
        if (productRepository.count() == 0) {
            List<Product> sampleProducts = List.of(
                    Product.builder()
                            .name("Hộp Flashcard 1000 Từ Vựng IELTS Core")
                            .price(149000.0)
                            .category("Thẻ Flashcard Giấy")
                            .description("Bộ thẻ flashcard giấy cứng cao cấp 300gsm in màu 2 mặt, kèm mã QR phát âm và ví dụ ngữ cảnh thực tế.")
                            .imageUrl("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=60")
                            .stockQuantity(50)
                            .isPhysical(true)
                            .build(),

                    Product.builder()
                            .name("Hộp Flashcard 800 Từ Vựng TOEIC Căn Bản")
                            .price(119000.0)
                            .category("Thẻ Flashcard Giấy")
                            .description("Tuyển tập 800 từ vựng xuất hiện nhiều nhất trong đề thi TOEIC format mới, phân loại theo chủ đề kinh doanh.")
                            .imageUrl("https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60")
                            .stockQuantity(70)
                            .isPhysical(true)
                            .build(),

                    Product.builder()
                            .name("Gói Nâng Cấp Tài Khoản VIP PRO (1 Năm)")
                            .price(499000.0)
                            .category("Gói VIP Số")
                            .description("Mở khóa toàn bộ kho từ vựng độc quyền, thuật toán SM-2 không giới hạn và gia sư AI phản hồi tức thì trong 365 ngày.")
                            .imageUrl("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=500&auto=format&fit=crop&q=60")
                            .stockQuantity(9999)
                            .isPhysical(false)
                            .build(),

                    Product.builder()
                            .name("Gói 1000 AI Tutor Credits")
                            .price(89000.0)
                            .category("Tài Nguyên AI")
                            .description("Nạp 1000 lượt chấm phát âm, sửa bài viết luận và luyện nói tiếng Anh 1-1 chuyên sâu cùng trợ lý AI.")
                            .imageUrl("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=60")
                            .stockQuantity(9999)
                            .isPhysical(false)
                            .build()
            );

            productRepository.saveAll(sampleProducts);
            log.info(" [ECOMMERCE SEEDER] Đã khởi tạo 4 sản phẩm mẫu cho Lab 04!");
        }
    }
}
