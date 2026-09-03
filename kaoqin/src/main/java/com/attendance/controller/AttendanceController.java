package com.attendance.controller;

import com.attendance.entity.*;
import com.attendance.repository.AttendanceRecordRepository;
import com.attendance.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final SystemConfigService systemConfigService;
    private final AttendanceRecordRepository attendanceRecordRepository;

    // 打卡页面
    @GetMapping("/attendance")
    public String attendancePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("todayRecord", attendanceService.getTodayAttendance(user));
        model.addAttribute("workStartTime", systemConfigService.getWorkStartTime());
        model.addAttribute("workEndTime", systemConfigService.getWorkEndTime());
        return "attendance";
    }

    @PostMapping("/checkin")
    public String checkIn(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            attendanceService.checkIn(user);
            redirectAttributes.addFlashAttribute("success", "上班打卡成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/attendance";
    }

    @PostMapping("/checkout")
    public String checkOut(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            attendanceService.checkOut(user);
            redirectAttributes.addFlashAttribute("success", "下班打卡成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/attendance";
    }

    // 我的考勤
    @GetMapping("/my-attendance")
    public String myAttendance(HttpSession session, Model model,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        List<AttendanceRecord> records = attendanceService.getUserAttendance(user, start, end);

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("records", records);
        return "my-attendance";
    }

    // 请假申请页面
    @GetMapping("/leave/apply")
    public String applyLeavePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("leaveRequest", new LeaveRequest());
        return "leave-apply";
    }

    @PostMapping("/leave/apply")
    public String applyLeave(@ModelAttribute LeaveRequest leaveRequest,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            leaveRequest.setUser(user);
            leaveService.applyLeave(leaveRequest);
            redirectAttributes.addFlashAttribute("success", "请假申请已提交，等待审批");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-leaves";
    }

    // 我的请假记录
    @GetMapping("/my-leaves")
    public String myLeaves(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<LeaveRequest> leaves = leaveService.getUserLeaves(user);
        model.addAttribute("leaves", leaves);
        return "my-leaves";
    }

    // 主管：待审批请假
    @GetMapping("/exec/pending-leaves")
    public String pendingLeaves(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<LeaveRequest> pendingLeaves;
        if ("ADMIN".equals(user.getRole())) {
            pendingLeaves = leaveService.getAllPendingLeaves();
        } else {
            pendingLeaves = leaveService.getPendingLeavesByDepartment(user.getDepartment());
        }

        model.addAttribute("pendingLeaves", pendingLeaves);
        return "exec/pending-leaves";
    }

    @PostMapping("/exec/approve/{leaveId}")
    public String approveLeave(@PathVariable Long leaveId,
                               @RequestParam(required = false) String comment,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            leaveService.approveLeave(leaveId, comment, user);
            redirectAttributes.addFlashAttribute("success", "已批准请假申请");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/exec/pending-leaves";
    }

    @PostMapping("/exec/reject/{leaveId}")
    public String rejectLeave(@PathVariable Long leaveId,
                              @RequestParam(required = false) String comment,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            leaveService.rejectLeave(leaveId, comment, user);
            redirectAttributes.addFlashAttribute("success", "已拒绝请假申请");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/exec/pending-leaves";
    }
    // 主管：查看部门考勤
    @GetMapping("/exec/team-attendance")
    public String teamAttendance(HttpSession session, Model model,
                                 @RequestParam(required = false) String username,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 只有主管可以访问
        if (!"EXEC".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        // 默认日期范围：从2000年开始到当前（确保查到所有数据）
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.of(2000, 1, 1);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : LocalDate.now();

        // 主管只能看自己部门的考勤
        List<AttendanceRecord> records = attendanceService.getAttendanceByRole(user, null, username, start, end);

        model.addAttribute("departmentName", user.getDepartment());
        model.addAttribute("selectedUsername", username);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("records", records);

        return "exec/team-attendance";
    }
}