package com.hrapp.employee_management.repository;

import com.hrapp.employee_management.model.LeaveRequest;
import com.hrapp.employee_management.model.LeaveRequest.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeUsername(String username);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    List<LeaveRequest> findByEmployeeUsernameAndStatus(String username, LeaveStatus status);
    List<LeaveRequest> findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            List<LeaveStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );


}
