package com.ecommerce.config;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import com.ecommerce.enums.Role;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CartRepository cartRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            User admin = new User("Admin", "admin@ecommerce.com",
                    passwordEncoder.encode("admin123456"));
            admin.setRole(Role.ROLE_ADMIN);
            admin = userRepository.save(admin);
            cartRepository.save(new Cart(admin));
            logger.info("Default admin account created (admin@ecommerce.com)");
        }
    }
}
