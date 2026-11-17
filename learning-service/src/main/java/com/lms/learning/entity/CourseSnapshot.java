package com.lms.learning.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_snapshots")
public class CourseSnapshot extends PanacheEntityBase {

    @Id
    @Column(name = "course_id")
    public UUID courseId;

    @Column(nullable = false)
    public String title;

    @Column(name = "instructor_id", nullable = false)
    public UUID instructorId;

    @Column(nullable = false)
    public BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    public String status;

    @Column(name = "total_lessons", nullable = false)
    public int totalLessons = 0;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt = OffsetDateTime.now();
}