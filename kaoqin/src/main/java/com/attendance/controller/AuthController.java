package com.attendance.controller;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.User;
import com.attendance.service.AttendanceService;
import com.attendance.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AttendanceService attendanceService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.findByUsername(username);
            if (!"APPROVED".equals(user.getStatus())) {
                model.addAttribute("error", "账号未通过审核，请联系管理员");
                return "login";
            }
            if (userService.checkPassword(password, user.getPassword())) {
                session.setAttribute("user", user);
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "密码错误");
                return "login";
            }
        } catch (RuntimeException e) {
            model.addAttribute("error", "用户不存在");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            // 设置默认状态
            user.setStatus("PENDING");
            userService.register(user);
            redirectAttributes.addFlashAttribute("success", "注册成功，请等待管理员审核");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        AttendanceRecord todayRecord = attendanceService.getTodayAttendance(user);
        model.addAttribute("todayRecord", todayRecord);
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}