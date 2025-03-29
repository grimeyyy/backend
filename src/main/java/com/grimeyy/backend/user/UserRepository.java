package com.grimeyy.backend.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grimeyy.backend.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
