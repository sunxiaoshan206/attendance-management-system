package com.attendance.repository;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByUserAndDate(User user, LocalDate date);

    List<AttendanceRecord> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<AttendanceRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.user.department = :department " +
            "AND a.user.role != 'ADMIN' AND a.user.role != 'MANAGER' " +
            "AND (:username IS NULL OR :username = '' OR a.user.username LIKE CONCAT('%', :username, '%') OR a.user.realName LIKE CONCAT('%', :username, '%')) " +
            "AND (:startDate IS NULL OR a.date >= :startDate) " +
            "AND (:endDate IS NULL OR a.date <= :endDate)"+
            "ORDER BY a.date ASC")
    List<AttendanceRecord> findDepartmentAttendance(@Param("department") String department,
                                                    @Param("username") String username,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE " +
            "(:department IS NULL OR :department = '' OR a.user.department = :department) " +
            "AND (:username IS NULL OR :username = '' OR a.user.username LIKE CONCAT('%', :username, '%') OR a.user.realName LIKE CONCAT('%', :username, '%')) " +
            "AND (:startDate IS NULL OR a.date >= :startDate) " +
            "AND (:endDate IS NULL OR a.date <= :endDate)"+
            "ORDER BY a.date ASC, a.user.realName")
    List<AttendanceRecord> findAllAttendance(@Param("department") String department,
                                             @Param("username") String username,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT a.user.department FROM AttendanceRecord a WHERE a.user.department IS NOT NULL AND a.user.department != ''")
    List<String> findAllDepartments();
}