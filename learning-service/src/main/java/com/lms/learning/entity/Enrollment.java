package com.lms.learning.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
public class Enrollment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "student_id", nullable = false)
    public UUID studentId;

    @Column(name = "student_name", nullable = false)
    public String studentName;

    @Column(name = "course_id", nullable = false)
    public UUID courseId;

    @Column(name = "course_title", nullable = false)
    public String courseTitle;

    @Column(nullable = false)
    public String status = "ACTIVE"; // ACTIVE, PENDING_PAYMENT, COMPLETED

    @Column(name = "progress_percentage", nullable = false)
    public int progressPercentage = 0;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    public OffsetDateTime enrolledAt = OffsetDateTime.now();

    @Column(name = "completed_at")
    public OffsetDateTime completedAt;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    public List<LessonProgress> completedLessons = new ArrayList<>();
}