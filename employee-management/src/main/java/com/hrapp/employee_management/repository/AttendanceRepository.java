package com.hrapp.employee_management.repository;

import com.hrapp.employee_management.model.Attendance;
import com.hrapp.employee_management.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployee_Id(Long employeeId);
    Optional<Attendance> findByEmployee_IdAndDate(Long employeeId, LocalDate date);
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.employee.id = :userId")
    void deleteByEmployeeId(@Param("userId") Long userId);

}

