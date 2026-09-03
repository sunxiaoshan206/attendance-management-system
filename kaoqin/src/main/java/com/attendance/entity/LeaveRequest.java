package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "leave_request")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private String reason;

    private String type;

    private String status;

    @Column(name = "manager_comment")
    private String managerComment;

    @CreationTimestamp
    @Column(name = "apply_time", updatable = false)
    private LocalDateTime applyTime;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;
}