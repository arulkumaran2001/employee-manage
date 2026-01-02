package com.hrapp.employee_management.controller;

import com.hrapp.employee_management.model.Attendance;
import com.hrapp.employee_management.model.AttendanceStatus;
import com.hrapp.employee_management.security.CustomUserDetails;
import com.hrapp.employee_management.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // ---------------- Employee Endpoints ----------------
    @PostMapping("/mark")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Attendance> markAttendance(
            Authentication authentication,
            @RequestBody Map<String, String> request
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long employeeId = userDetails.getUser().getId();
        AttendanceStatus status = AttendanceStatus.valueOf(request.get("status").toUpperCase());

        Attendance attendance = attendanceService.markAttendance(employeeId, LocalDate.now(), status);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<Map<String, Object>>> getMyAttendance(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long employeeId = userDetails.getUser().getId();

        return ResponseEntity.ok(attendanceService.getAttendanceByEmployee(employeeId));
    }

    // ---------------- Admin & HR Endpoints ----------------
    @PostMapping("/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Attendance> overrideAttendance(@RequestBody Map<String, String> request) {
        Long employeeId = Long.parseLong(request.get("employeeId"));
        LocalDate date = LocalDate.parse(request.get("date"));
        AttendanceStatus status = AttendanceStatus.valueOf(request.get("status").toUpperCase());
        String reason = request.getOrDefault("reason", "Updated by Admin");

        Attendance attendance = attendanceService.overrideAttendance(employeeId, date, status, reason);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployee(employeeId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }
}
