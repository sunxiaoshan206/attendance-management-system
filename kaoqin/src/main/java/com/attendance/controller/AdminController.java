package com.attendance.controller;

import com.attendance.entity.*;
import com.attendance.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final SystemConfigService systemConfigService;
    private final LeaveService leaveService;

    @GetMapping("/pending-users")
    public String pendingUsers(Model model) {
        List<User> pendingUsers = userService.findPendingUsers();
        model.addAttribute("pendingUsers", pendingUsers);
        return "admin/pending-users";
    }

    @PostMapping("/approve/{userId}")
    public String approveUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.approveUser(userId);
        redirectAttributes.addFlashAttribute("success", "用户审核通过");
        return "redirect:/admin/pending-users";
    }

    @PostMapping("/reject/{userId}")
    public String rejectUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        userService.rejectUser(userId);
        redirectAttributes.addFlashAttribute("success", "用户已拒绝");
        return "redirect:/admin/pending-users";
    }

    @GetMapping("/attendance-report")
    public String attendanceReport(HttpSession session, Model model,
                                   @RequestParam(required = false) String department,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        // 默认日期范围：从2000年开始到当前
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.of(2000, 1, 1);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

        List<AttendanceRecord> records = attendanceService.getAttendanceByRole(currentUser, department, username, start, end);
        List<String> departments = attendanceService.getAllDepartments();

        model.addAttribute("departments", departments);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedUsername", username);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("records", records);

        return "admin/attendance-report";
    }

    @GetMapping("/config")
    public String systemConfig(Model model) {
        LocalTime startTime = systemConfigService.getWorkStartTime();
        LocalTime endTime = systemConfigService.getWorkEndTime();

        model.addAttribute("startTime", startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        model.addAttribute("endTime", endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return "admin/config";
    }

    @PostMapping("/config")
    public String updateConfig(@RequestParam String workStartTime,
                               @RequestParam String workEndTime,
                               RedirectAttributes redirectAttributes) {
        try {
            systemConfigService.setWorkStartTime(LocalTime.parse(workStartTime));
            systemConfigService.setWorkEndTime(LocalTime.parse(workEndTime));
            redirectAttributes.addFlashAttribute("success", "配置更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "配置更新失败：" + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    @GetMapping("/leave-requests")
    public String allLeaveRequests(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 管理员或经理可以看所有请假
        if ("ADMIN".equals(user.getRole()) || "MANAGER".equals(user.getRole())) {
            List<LeaveRequest> allLeaves = leaveService.getAllLeaveRequests();
            model.addAttribute("leaves", allLeaves);
            return "admin/leave-requests";
        }

        return "redirect:/dashboard";
    }

    // 审批请假（管理员和经理都可以）
    @PostMapping("/approve-leave/{leaveId}")
    public String approveLeave(@PathVariable Long leaveId,
                               @RequestParam(required = false) String comment,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 只有管理员或经理可以审批
        if (!"ADMIN".equals(user.getRole()) && !"MANAGER".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "您没有权限审批");
            return "redirect:/dashboard";
        }

        try {
            leaveService.approveLeave(leaveId, comment, user);
            redirectAttributes.addFlashAttribute("success", "请假已批准");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leave-requests";
    }

    @PostMapping("/reject-leave/{leaveId}")
    public String rejectLeave(@PathVariable Long leaveId,
                              @RequestParam(required = false) String comment,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        if (!"ADMIN".equals(user.getRole()) && !"MANAGER".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "您没有权限审批");
            return "redirect:/dashboard";
        }

        try {
            leaveService.rejectLeave(leaveId, comment, user);
            redirectAttributes.addFlashAttribute("success", "请假已拒绝");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leave-requests";
    }

}