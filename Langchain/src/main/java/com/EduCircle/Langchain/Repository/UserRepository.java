package com.EduCircle.Langchain.Repository;

import com.EduCircle.Langchain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);   // ✅ correct

    List<User> findByLastActiveBefore(LocalDate date);

    List<User> findByIsActiveTrue();
}