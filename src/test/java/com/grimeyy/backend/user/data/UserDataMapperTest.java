package com.grimeyy.backend.user.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
public class UserDataMapperTest {
	 private final UserDataMapper mapper = new UserDataMapper();
	 
	 @Test
	 void toEntity_shouldMapDtoToEntityCorrectly() {
		 UserDataDto dto = new UserDataDto("Anna", "Hauptstraße 5", "987654321");

	     UserData user = mapper.toEntity(dto);

	     assertEquals("Anna", user.getName());
	     assertEquals("Hauptstraße 5", user.getAddress());
	     assertEquals("987654321", user.getPhoneNumber());
	 }

	 @Test
	 void toDto_shouldMapEntityToDtoCorrectly() {
		 UserData user = new UserData();
	     user.setName("Max");
	     user.setAddress("Musterstraße 1");
	     user.setPhoneNumber("123456789");

	     UserDataDto dto = mapper.toDto(user);

	     assertEquals("Max", dto.getName());
	     assertEquals("Musterstraße 1", dto.getAddress());
	     assertEquals("123456789", dto.getPhoneNumber());
	 }

}  
