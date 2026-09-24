package com.zhixu.studycompass.config;

import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.demo-account.enabled", havingValue = "true", matchIfMissing = true)
public class DemoAccountInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DemoAccountInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder,
                                  @Value("${app.demo-account.password:Study2026}") String demoPassword) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(String... args) {
        User user = userMapper.findByUsername("demo_user").orElse(null);
        if (user != null && user.getPasswordHash() == null) {
            userMapper.updatePasswordHash(user.getId(), passwordEncoder.encode(demoPassword));
        }
    }
}
