package com.quang.marketplace.modules.identity;

import com.quang.marketplace.modules.identity.domain.User;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_save_and_find_user_by_email() {
        User user = new User("test@example.com", "hashed_password");

        userRepository.save(user);

        var found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}