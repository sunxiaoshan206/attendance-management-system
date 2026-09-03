package com.attendance.service;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.User;
import com.attendance.repository.AttendanceRecordRepository;
import com.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SystemConfigService systemConfigService;
    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional
    public AttendanceRecord checkIn(User user) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime workStart = systemConfigService.getWorkStartTime();

        AttendanceRecord record = attendanceRecordRepository.findByUserAndDate(user, today)
                .orElse(new AttendanceRecord());

        if (record.getId() == null) {
            record.setUser(user);
            record.setDate(today);
        }

        record.setCheckInTime(now);

        if (now.isAfter(workStart)) {
            record.setStatus("LATE");
            record.setLateMinutes((int) java.time.Duration.between(workStart, now).toMinutes());
        } else {
            if (!"LATE".equals(record.getStatus())) {
                record.setStatus("ON_TIME");
            }
        }

        return attendanceRecordRepository.save(record);
    }

    @Transactional
    public AttendanceRecord checkOut(User user) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime workEnd = systemConfigService.getWorkEndTime();

        AttendanceRecord record = attendanceRecordRepository.findByUserAndDate(user, today)
                .orElseThrow(() -> new RuntimeException("今日未打卡上班"));

        record.setCheckOutTime(now);

        if (now.isBefore(workEnd)) {
            record.setEarlyLeaveMinutes((int) java.time.Duration.between(now, workEnd).toMinutes());
            if (!"LATE".equals(record.getStatus())) {
                record.setStatus("EARLY_LEAVE");
            }
        }

        return attendanceRecordRepository.save(record);
    }

    public List<AttendanceRecord> getUserAttendance(User user, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByUserAndDateBetween(user, startDate, endDate);
        return markLeaveStatus(records, user, startDate, endDate);
    }

    public AttendanceRecord getTodayAttendance(User user) {
        return attendanceRecordRepository.findByUserAndDate(user, LocalDate.now()).orElse(null);
    }

    // 标记请假状态
    private List<AttendanceRecord> markLeaveStatus(List<AttendanceRecord> records, User user, LocalDate startDate, LocalDate endDate) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        List<LeaveRequest> leaves = leaveRequestRepository.findApprovedLeavesByUserAndDateRange(user, startDate, endDate);
        for (AttendanceRecord record : records) {
            for (LeaveRequest leave : leaves) {
                if (!record.getDate().isBefore(leave.getStartDate()) && !record.getDate().isAfter(leave.getEndDate())) {
                    record.setStatus("LEAVE");
                    break;
                }
            }
        }
        return records;
    }

    // 根据角色查询考勤（权限控制）
    public List<AttendanceRecord> getAttendanceByRole(User currentUser, String department, String username,
                                                      LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.of(2000, 1, 1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<AttendanceRecord> records;

        // 管理员 或 经理：可以看全公司
        if ("ADMIN".equals(currentUser.getRole()) || "MANAGER".equals(currentUser.getRole())) {
            records = attendanceRecordRepository.findAllAttendance(department, username, startDate, endDate);
            // 为每条记录标记请假状态
            for (AttendanceRecord r : records) {
                List<LeaveRequest> leaves = leaveRequestRepository.findApprovedLeavesByUserAndDateRange(r.getUser(), startDate, endDate);
                for (LeaveRequest leave : leaves) {
                    if (!r.getDate().isBefore(leave.getStartDate()) && !r.getDate().isAfter(leave.getEndDate())) {
                        r.setStatus("LEAVE");
                        break;
                    }
                }
            }
            return records;
        }

        // 主管：只能看自己部门的员工
        if ("EXEC".equals(currentUser.getRole())) {
            String userDept = currentUser.getDepartment();
            records = attendanceRecordRepository.findDepartmentAttendance(userDept, username, startDate, endDate);
            return markLeaveStatus(records, currentUser, startDate, endDate);
        }

        // 员工：只能看自己的考勤
        records = attendanceRecordRepository.findByUserAndDateBetween(currentUser, startDate, endDate);
        return markLeaveStatus(records, currentUser, startDate, endDate);
    }

    public List<String> getAllDepartments() {
        return attendanceRecordRepository.findAllDepartments();
    }
}