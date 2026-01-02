package com.hrapp.employee_management.service;

import com.hrapp.employee_management.model.Attendance;
import com.hrapp.employee_management.model.AttendanceStatus;
import com.hrapp.employee_management.model.LeaveRequest;
import com.hrapp.employee_management.model.User;
import com.hrapp.employee_management.repository.AttendanceRepository;
import com.hrapp.employee_management.repository.LeaveRequestRepository;
import com.hrapp.employee_management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             LeaveRequestRepository leaveRequestRepository,
                             UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Attendance markAttendance(Long employeeId, LocalDate date, AttendanceStatus status) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        boolean leaveApproved = !leaveRequestRepository
                .findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId,
                        Collections.singletonList(LeaveRequest.LeaveStatus.APPROVED),
                        date,
                        date
                ).isEmpty();


        if (leaveApproved) {
            throw new RuntimeException("Attendance cannot be marked. Approved leave exists for today.");
        }

        if (attendanceRepository.findByEmployee_IdAndDate(employeeId, date).isPresent()) {
            throw new RuntimeException("Attendance already marked for today.");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(date);
        attendance.setStatus(status);

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance overrideAttendance(Long employeeId, LocalDate date, AttendanceStatus status, String reason) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = attendanceRepository.findByEmployee_IdAndDate(employeeId, date)
                .orElseGet(() -> {
                    Attendance newAttendance = new Attendance();
                    newAttendance.setEmployee(employee);
                    newAttendance.setDate(date);
                    return newAttendance;
                });

        attendance.setStatus(status);
        attendance.setReason(reason);

        return attendanceRepository.save(attendance);
    }

    public List<Map<String, Object>> getAttendanceByEmployee(Long employeeId) {
        return buildAttendanceWithLeave(employeeId);
    }

    public List<Map<String, Object>> getAllAttendance() {
        return userRepository.findAll().stream()
                .flatMap(user -> buildAttendanceWithLeave(user.getId(), true).stream())
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildAttendanceWithLeave(Long employeeId) {
        return buildAttendanceWithLeave(employeeId, false);
    }

    // Admin version (today included even if not marked)
    private List<Map<String, Object>> buildAttendanceWithLeave(Long employeeId, boolean showTodayForAdmin) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        List<Attendance> attendanceList = attendanceRepository.findByEmployee_Id(employeeId);
        List<LeaveRequest> leaveList = leaveRequestRepository.findByEmployeeIdAndStatus(
                employeeId, LeaveRequest.LeaveStatus.APPROVED);

        Map<LocalDate, AttendanceStatus> records = new HashMap<>();
        for (Attendance a : attendanceList) {
            records.put(a.getDate(), a.getStatus());
        }
        for (LeaveRequest leave : leaveList) {
            LocalDate d = leave.getStartDate();
            while (!d.isAfter(leave.getEndDate())) {
                records.put(d, AttendanceStatus.LEAVE);
                d = d.plusDays(1);
            }
        }

        User employee = userRepository.findById(employeeId).orElse(null);
        List<Map<String, Object>> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            if (date.equals(today) && !records.containsKey(date) && !showTodayForAdmin) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("status", records.getOrDefault(date, AttendanceStatus.NOT_MARKED));

            if (employee != null) {
                map.put("employeeId", employee.getId());
                map.put("employeeName", employee.getName());
                map.put("employeeEmail", employee.getEmail());
            }

            result.add(map);
        }

        return result;
    }
}
