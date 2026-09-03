package com.attendance.repository;

import com.attendance.entity.LeaveRequest;
import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUser(User user);

    List<LeaveRequest> findByStatus(String status);

    // 查询用户在某时间段内的已批准请假
    @Query("SELECT l FROM LeaveRequest l WHERE l.user = :user " +
            "AND l.status = 'APPROVED' " +
            "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<LeaveRequest> findApprovedLeavesByUserAndDateRange(@Param("user") User user,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    // 主管查询本部门待审批请假
    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'PENDING' AND l.user.department = :department")
    List<LeaveRequest> findPendingLeavesByDepartment(@Param("department") String department);

    // 管理员：查询所有待审批请假
    List<LeaveRequest> findByStatusOrderByApplyTimeDesc(String status);
}