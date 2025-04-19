package com.grimeyy.backend.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.grimeyy.backend.user.data.UserData;
import com.grimeyy.backend.user.data.UserDataRepository;

import java.util.Optional;

@DataJpaTest
class UserDataRepositoryTest {

    @Autowired
    private UserDataRepository userDataRepository;

    @Test
    void testSaveAndFindUserData() {

        UserData userData = new UserData();
        userData.setEmail("test@example.com");
        userData.setPassword("password123");
        userData.setName("Max Mustermann");
        userData.setAddress("Musterstraße 1");
        userData.setPhoneNumber("0123456789");

        UserData savedUser = userDataRepository.save(userData);
        
        Optional<UserData> foundUser = userDataRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getName()).isEqualTo("Max Mustermann");
    }
}

