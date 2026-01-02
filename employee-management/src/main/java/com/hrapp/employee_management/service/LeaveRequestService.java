package com.hrapp.employee_management.service;

import com.hrapp.employee_management.model.LeaveRequest;
import com.hrapp.employee_management.model.LeaveRequest.LeaveStatus;
import com.hrapp.employee_management.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public LeaveRequest applyLeave(LeaveRequest leaveRequest) {
        LocalDate today = LocalDate.now();

        if (leaveRequest.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException("Leave cannot start in the past.");
        }

        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        List<LeaveRequest> overlappingLeaves = leaveRequestRepository
                .findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        leaveRequest.getEmployeeId(),
                        Arrays.asList(LeaveStatus.APPROVED, LeaveStatus.PENDING),
                        leaveRequest.getEndDate(),
                        leaveRequest.getStartDate()
                );

        if (!overlappingLeaves.isEmpty()) {
            throw new IllegalArgumentException("Overlapping leave request already exists.");
        }

        leaveRequest.setStatus(LeaveStatus.PENDING);
        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getEmployeeLeavesByUsername(String username) {
        return leaveRequestRepository.findByEmployeeUsername(username);
    }

    public List<LeaveRequest> getEmployeeLeavesByUsernameAndStatus(String username, LeaveStatus status) {
        return leaveRequestRepository.findByEmployeeUsernameAndStatus(username, status);
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getLeavesByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }

    public Optional<LeaveRequest> updateLeaveStatus(Long id, LeaveStatus status) {
        Optional<LeaveRequest> leaveOpt = leaveRequestRepository.findById(id);
        leaveOpt.ifPresent(leave -> {
            leave.setStatus(status);
            leaveRequestRepository.save(leave);
        });
        return leaveOpt;
    }
}
