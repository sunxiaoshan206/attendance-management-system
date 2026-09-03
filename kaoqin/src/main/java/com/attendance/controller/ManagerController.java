package com.attendance.controller;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.User;
import com.attendance.service.AttendanceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final AttendanceService attendanceService;

    @GetMapping("/all-attendance")
    public String allAttendance(HttpSession session, Model model,
                                @RequestParam(required = false) String department,
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        if (!"MANAGER".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        // 处理空字符串
        if (department != null && department.isEmpty()) department = null;
        if (username != null && username.isEmpty()) username = null;

        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.of(2000, 1, 1);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

        List<AttendanceRecord> records = attendanceService.getAttendanceByRole(user, department, username, start, end);
        List<String> departments = attendanceService.getAllDepartments();

        model.addAttribute("departments", departments);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedUsername", username);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("records", records);

        return "manager/all-attendance";
    }
}