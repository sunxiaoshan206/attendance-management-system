package com.attendance.service;

import com.attendance.entity.LeaveRequest;
import com.attendance.entity.User;
import com.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional
    public LeaveRequest applyLeave(LeaveRequest leaveRequest) {
        leaveRequest.setStatus("PENDING");
        leaveRequest.setApplyTime(LocalDateTime.now());
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest approveLeave(Long leaveId, String comment, User approver) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));

        // 经理（MANAGER）可以审批任何人的请假（包括管理员）
        if ("MANAGER".equals(approver.getRole())) {
            leave.setStatus("APPROVED");
            leave.setManagerComment(comment);
            leave.setReviewTime(LocalDateTime.now());
            return leaveRequestRepository.save(leave);
        }

        // 管理员可以审批任何人的请假
        if ("ADMIN".equals(approver.getRole())) {
            leave.setStatus("APPROVED");
            leave.setManagerComment(comment);
            leave.setReviewTime(LocalDateTime.now());
            return leaveRequestRepository.save(leave);
        }

        // 主管只能审批自己部门的员工
        if ("EXEC".equals(approver.getRole())) {
            if (!approver.getDepartment().equals(leave.getUser().getDepartment())) {
                throw new RuntimeException("您只能审批本部门员工的请假申请");
            }
            leave.setStatus("APPROVED");
            leave.setManagerComment(comment);
            leave.setReviewTime(LocalDateTime.now());
            return leaveRequestRepository.save(leave);
        }

        throw new RuntimeException("您没有权限审批");
    }

    @Transactional
    public LeaveRequest rejectLeave(Long leaveId, String comment, User approver) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));

        // 经理可以拒绝任何人的请假
        if ("MANAGER".equals(approver.getRole())) {
            leave.setStatus("REJECTED");
            leave.setManagerComment(comment);
            leave.setReviewTime(LocalDateTime.now());
            return leaveRequestRepository.save(leave);
        }

        // 管理员可以拒绝任何人的请假
        if ("ADMIN".equals(approver.getRole())) {
            leave.setStatus("REJECTED");
            leave.setManagerComment(comment);
            leave.setReviewTime(LocalDateTime.now());
            return leaveRequestRepository.save(leave);
        }

        // 主管只能拒绝自己部门的员工
        if ("EXEC".equals(approver.getRole())) {
            if (!approver.getDepartment().equals(leave.getUser().getDepartment())) {
                throw new RuntimeException("您只能审批本部门员工的请假申请");
            }
            leave.setStatus("REJECTED");
            leave.setManagerComment(comment);
            leave.setReviewTime(LocalDateTime.now());
            return leaveRequestRepository.save(leave);
        }

        throw new RuntimeException("您没有权限审批");
    }

    public List<LeaveRequest> getUserLeaves(User user) {
        return leaveRequestRepository.findByUser(user);
    }

    public List<LeaveRequest> getPendingLeavesByDepartment(String department) {
        return leaveRequestRepository.findPendingLeavesByDepartment(department);
    }

    public List<LeaveRequest> getAllPendingLeaves() {
        return leaveRequestRepository.findByStatus("PENDING");
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }
}