package com.jldemiguel.authserver.repository;

import com.jldemiguel.authserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.username = :value OR u.email = :value")
    User findByUsernameOrEmail(@Param("value") String value);
}
