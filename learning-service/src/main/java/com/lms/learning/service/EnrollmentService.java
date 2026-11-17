package com.lms.learning.service;

import com.lms.learning.dto.CompleteLessonRequest;
import com.lms.learning.entity.CourseSnapshot;
import com.lms.learning.entity.Enrollment;
import com.lms.learning.entity.LessonProgress;
import com.lms.learning.event.LearningEvent;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EnrollmentService {

    @Inject
    @Channel("learning-events-out")
    Emitter<Record<String, LearningEvent>> learningEventEmitter;

    public List<CourseSnapshot> listAvailableCourses() {
        return CourseSnapshot.listAll();
    }

    public List<Enrollment> getMyEnrollments(UUID studentId) {
        return Enrollment.list("studentId", studentId);
    }

    @Transactional
    public Enrollment enroll(UUID courseId, UUID studentId, String studentName) {
        CourseSnapshot course = CourseSnapshot.findById(courseId);
        if (course == null) {
            throw new NotFoundException("Kursus tidak ditemukan di katalog pembelajaran");
        }
        if (!"PUBLISHED".equalsIgnoreCase(course.status) && !"ACTIVE".equalsIgnoreCase(course.status)) {
            throw new BadRequestException("Kursus belum dipublikasikan (status: " + course.status + ")");
        }

        Enrollment existing = Enrollment.find("studentId = ?1 and courseId = ?2", studentId, courseId).firstResult();
        if (existing != null) {
            return existing;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.studentId = studentId;
        enrollment.studentName = (studentName != null) ? studentName : "Student";
        enrollment.courseId = course.courseId;
        enrollment.courseTitle = course.title;
        enrollment.status = (course.price.compareTo(BigDecimal.ZERO) == 0) ? "ACTIVE" : "PENDING_PAYMENT";
        enrollment.progressPercentage = 0;
        enrollment.enrolledAt = OffsetDateTime.now();
        enrollment.persist();

        emitEvent("STUDENT_ENROLLED", enrollment);
        return enrollment;
    }

    @Transactional
    public Enrollment markLessonCompleted(UUID courseId, UUID studentId, CompleteLessonRequest req) {
        Enrollment enrollment = Enrollment.find("studentId = ?1 and courseId = ?2", studentId, courseId).firstResult();
        if (enrollment == null) {
            throw new NotFoundException("Anda belum terdaftar pada kursus ini");
        }
        if ("PENDING_PAYMENT".equals(enrollment.status)) {
            throw new BadRequestException("Selesaikan pembayaran terlebih dahulu sebelum mengakses materi");
        }

        boolean alreadyDone = enrollment.completedLessons.stream()
                .anyMatch(lp -> lp.lessonId.equals(req.lessonId()));

        if (!alreadyDone) {
            LessonProgress progress = new LessonProgress();
            progress.enrollment = enrollment;
            progress.lessonId = req.lessonId();
            progress.completedAt = OffsetDateTime.now();
            progress.persist();
            enrollment.completedLessons.add(progress);
        }

        int completedCount = enrollment.completedLessons.size();
        int percentage = Math.min(100, (int) Math.round((completedCount * 100.0) / req.totalLessons()));
        enrollment.progressPercentage = percentage;

        if (percentage >= 100 && !"COMPLETED".equals(enrollment.status)) {
            enrollment.status = "COMPLETED";
            enrollment.completedAt = OffsetDateTime.now();
            emitEvent("COURSE_COMPLETED", enrollment);
        }

        return enrollment;
    }

    private void emitEvent(String eventType, Enrollment enrollment) {
        LearningEvent event = new LearningEvent(
                eventType,
                enrollment.id,
                enrollment.studentId,
                enrollment.studentName,
                enrollment.courseId,
                enrollment.courseTitle,
                enrollment.progressPercentage,
                OffsetDateTime.now()
        );
        learningEventEmitter.send(Record.of(enrollment.id.toString(), event));
    }
}