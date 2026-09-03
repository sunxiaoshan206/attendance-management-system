package com.attendance.repository;

import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByStatus(String status);
    List<User> findByRoleAndStatus(String role, String status);
    boolean existsByUsername(String username);
    List<User> findByDepartment(String department);
}