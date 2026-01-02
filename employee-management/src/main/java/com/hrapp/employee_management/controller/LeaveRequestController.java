package com.hrapp.employee_management.controller;

import com.hrapp.employee_management.model.LeaveRequest;
import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.service.LeaveRequestService;
import com.hrapp.employee_management.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "*") // Adjust in production
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final UserService userService;

    public LeaveRequestController(LeaveRequestService leaveRequestService, UserService userService) {
        this.leaveRequestService = leaveRequestService;
        this.userService = userService;
    }

    // ------------------ EMPLOYEE ------------------

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> applyLeave(Authentication authentication,
                                        @RequestBody LeaveRequest leaveRequest) {
        try {
            String username = authentication.getName();
            User employee = userService.findByEmail(username);

            leaveRequest.setEmployeeUsername(username);
            leaveRequest.setEmployeeId(employee.getId());
            leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);

            LeaveRequest createdLeave = leaveRequestService.applyLeave(leaveRequest);
            return ResponseEntity.ok(Map.of(
                    "message", "Leave applied successfully",
                    "leave", createdLeave
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to apply leave",
                    "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getMyLeaves(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<LeaveRequest> leaves = leaveRequestService.getEmployeeLeavesByUsername(username);
            return ResponseEntity.ok(leaves);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch leave requests",
                    "details", e.getMessage()
            ));
        }
    }

    // ------------------ HR / ADMIN ------------------

    @GetMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllLeaves(@RequestParam(required = false) String status) {
        try {
            List<LeaveRequest> leaves;
            if (status != null) {
                LeaveRequest.LeaveStatus enumStatus;
                try {
                    enumStatus = LeaveRequest.LeaveStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value"));
                }
                leaves = leaveRequestService.getAllLeaves().stream()
                        .filter(l -> l.getStatus() == enumStatus)
                        .toList();
            } else {
                leaves = leaveRequestService.getAllLeaves();
            }
            return ResponseEntity.ok(leaves);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch leave requests",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateLeaveStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing status in request body"));
        }

        LeaveRequest.LeaveStatus enumStatus;
        try {
            enumStatus = LeaveRequest.LeaveStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value"));
        }

        return leaveRequestService.updateLeaveStatus(id, enumStatus)
                .map(lr -> ResponseEntity.ok(Map.of(
                        "message", "Leave status updated",
                        "leave", lr
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Leave request not found")));
    }
}
