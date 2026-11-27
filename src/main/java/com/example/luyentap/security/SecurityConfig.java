package com.example.luyentap.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) return false;
                // Nếu mật khẩu lưu trong DB là mã băm BCrypt
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }
                // Hỗ trợ kiểm tra mật khẩu dạng chuỗi thô cũ có sẵn trong Database
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Vô hiệu hóa CSRF vì dùng REST API không trạng thái với Token
                .csrf(AbstractHttpConfigurer::disable)
                // Kích hoạt cấu hình CORS mặc định
                .cors(Customizer.withDefaults())
                // Quản lý session ở chế độ STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Xử lý lỗi trả về JSON 401 khi chưa đăng nhập
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"Yêu cầu quyền truy cập hoặc phiên làm việc đã hết hạn!\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"Bạn không có quyền thực hiện thao tác này!\"}");
                        })
                )
                // Cấu hình phân quyền chi tiết cho từng endpoint
                .authorizeHttpRequests(auth -> auth
                        // 1. Toàn bộ file tĩnh (Static resources, HTML, CSS, JS, hình ảnh, game) được truy cập tự do
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/*.js",
                                "/*.css",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/gametuvung/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // 2. API xác thực đăng nhập / đăng ký
                        .requestMatchers("/api/auth/**").permitAll()

                        // 3. API học tập & tra cứu công khai (GET topics, vocabularies, quiz, ai, folders)
                        .requestMatchers(HttpMethod.GET, "/api/folders/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/topics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vocabularies/**").permitAll()
                        .requestMatchers("/api/quiz/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/api/progress/**", "/api/user-progress/**").permitAll()
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/api/shipping/**").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()

                        // 4. API quản trị yêu cầu quyền ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/folders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/folders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/folders/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/topics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/topics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/topics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/vocabularies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vocabularies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vocabularies/**").hasRole("ADMIN")

                        // Mọi request còn lại yêu cầu phải đăng nhập
                        .anyRequest().authenticated()
                );

        // Chèn JwtAuthenticationFilter vào trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
