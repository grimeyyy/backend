package com.grimeyy.backend.user.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataRepository extends JpaRepository<UserData, Long>  {

	Optional<UserData> findByEmail(String email);
}
