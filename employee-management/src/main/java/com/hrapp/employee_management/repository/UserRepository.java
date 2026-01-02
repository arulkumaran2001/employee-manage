package com.hrapp.employee_management.repository;

import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    List<User> findByRoleAndStatus(String role, UserStatus status);
    List<User> findByRole(String role);
    List<User> findByStatus(UserStatus status);
    List<User> findByRoleOrderByStatusAscNameAsc(String role);
}
