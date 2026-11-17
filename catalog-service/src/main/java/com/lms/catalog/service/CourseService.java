package com.lms.catalog.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.lms.catalog.dto.CourseRequest;
import com.lms.catalog.dto.LessonRequest;
import com.lms.catalog.dto.ModuleRequest;
import com.lms.catalog.entity.Course;
import com.lms.catalog.entity.CourseModule;
import com.lms.catalog.entity.Lesson;
import com.lms.catalog.event.CatalogEvent;

import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class CourseService {

    @Inject
    @Channel("catalog-events")
    Emitter<Record<String, CatalogEvent>> catalogEventEmitter;

    public List<Course> listAllCourses() {
        return Course.listAll();
    }

    public Course getCourseById(UUID id) {
        Course course = Course.findById(id);
        if (course == null) {
            throw new NotFoundException("Kursus dengan ID " + id + " tidak ditemukan");
        }
        return course;
    }

    @Transactional
    public Course createCourse(CourseRequest req, UUID instructorId, String instructorName) {
        Course course = new Course();
        course.title = req.title();
        course.description = req.description();
        course.instructorId = instructorId;
        course.instructorName = (instructorName != null) ? instructorName : "Instructor";
        course.price = (req.price() != null) ? req.price() : BigDecimal.ZERO;
        course.status = (req.status() != null && !req.status().isBlank()) ? req.status() : "DRAFT";
        course.createdAt = OffsetDateTime.now();
        course.updatedAt = OffsetDateTime.now();
        course.persist();

        emitEvent("COURSE_CREATED", course);
        return course;
    }

    @Transactional
    public Course updateCourse(UUID id, CourseRequest req, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(id);
        validateOwnership(course, requesterId, isAdmin);

        String previousStatus = course.status;
        course.title = req.title();
        course.description = req.description();
        if (req.price() != null) {
            course.price = req.price();
        }
        if (req.status() != null && !req.status().isBlank()) {
            course.status = req.status();
        }
        course.updatedAt = OffsetDateTime.now();

        String eventType = (!"PUBLISHED".equals(previousStatus) && "PUBLISHED".equals(course.status))
                ? "COURSE_PUBLISHED"
                : "COURSE_UPDATED";
        emitEvent(eventType, course);

        return course;
    }

    @Transactional
    public void deleteCourse(UUID id, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(id);
        validateOwnership(course, requesterId, isAdmin);
        emitEvent("COURSE_DELETED", course);
        course.delete();
    }

    @Transactional
    public CourseModule addModule(UUID courseId, ModuleRequest req, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(courseId);
        validateOwnership(course, requesterId, isAdmin);

        CourseModule module = new CourseModule();
        module.course = course;
        module.title = req.title();
        module.position = req.position();
        module.createdAt = OffsetDateTime.now();
        module.persist();

        course.modules.add(module);
        return module;
    }

    @Transactional
    public Lesson addLesson(UUID courseId, UUID moduleId, LessonRequest req, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(courseId);
        validateOwnership(course, requesterId, isAdmin);

        CourseModule module = CourseModule.findById(moduleId);
        if (module == null || !module.course.id.equals(courseId)) {
            throw new NotFoundException("Modul tidak ditemukan pada kursus ini");
        }

        Lesson lesson = new Lesson();
        lesson.module = module;
        lesson.title = req.title();
        lesson.contentType = (req.contentType() != null && !req.contentType().isBlank()) ? req.contentType() : "VIDEO";
        lesson.contentUrl = req.contentUrl();
        lesson.durationSeconds = req.durationSeconds();
        lesson.position = req.position();
        lesson.createdAt = OffsetDateTime.now();
        lesson.persist();

        module.lessons.add(lesson);
        return lesson;
    }

    @Transactional
    public Lesson updateLesson(UUID courseId, UUID moduleId, UUID lessonId, LessonRequest req, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(courseId);
        validateOwnership(course, requesterId, isAdmin);

        CourseModule module = CourseModule.findById(moduleId);
        if (module == null || !module.course.id.equals(courseId)) {
            throw new NotFoundException("Modul tidak ditemukan pada kursus ini");
        }

        Lesson lesson = Lesson.findById(lessonId);
        if (lesson == null || !lesson.module.id.equals(moduleId)) {
            throw new NotFoundException("Materi (lesson) tidak ditemukan pada modul ini");
        }

        lesson.title = req.title();
        if (req.contentType() != null && !req.contentType().isBlank()) {
            lesson.contentType = req.contentType();
        }
        lesson.contentUrl = req.contentUrl();
        lesson.durationSeconds = req.durationSeconds();
        lesson.position = req.position();
        return lesson;
    }

    @Transactional
    public void deleteLesson(UUID courseId, UUID moduleId, UUID lessonId, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(courseId);
        validateOwnership(course, requesterId, isAdmin);

        CourseModule module = CourseModule.findById(moduleId);
        if (module == null || !module.course.id.equals(courseId)) {
            throw new NotFoundException("Modul tidak ditemukan pada kursus ini");
        }

        Lesson lesson = Lesson.findById(lessonId);
        if (lesson == null || !lesson.module.id.equals(moduleId)) {
            throw new NotFoundException("Materi (lesson) tidak ditemukan pada modul ini");
        }

        module.lessons.remove(lesson);
        lesson.delete();
    }

    @Transactional
    public CourseModule updateModule(UUID courseId, UUID moduleId, ModuleRequest req, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(courseId);
        validateOwnership(course, requesterId, isAdmin);

        CourseModule module = CourseModule.findById(moduleId);
        if (module == null || !module.course.id.equals(courseId)) {
            throw new NotFoundException("Modul tidak ditemukan pada kursus ini");
        }

        module.title = req.title();
        module.position = req.position();
        return module;
    }

    @Transactional
    public void deleteModule(UUID courseId, UUID moduleId, UUID requesterId, boolean isAdmin) {
        Course course = getCourseById(courseId);
        validateOwnership(course, requesterId, isAdmin);

        CourseModule module = CourseModule.findById(moduleId);
        if (module == null || !module.course.id.equals(courseId)) {
            throw new NotFoundException("Modul tidak ditemukan pada kursus ini");
        }

        course.modules.remove(module);
        module.delete();
    }

    private void emitEvent(String eventType, Course course) {
        int totalLessons = course.modules.stream()
                .mapToInt(module -> module.lessons.size())
                .sum();

        CatalogEvent event = new CatalogEvent(
                eventType,
                course.id,
                course.title,
                course.instructorId,
                course.price,
                course.status,
                totalLessons,
                OffsetDateTime.now()
        );
        catalogEventEmitter.send(Record.of(course.id.toString(), event));
    }

    private void validateOwnership(Course course, UUID requesterId, boolean isAdmin) {
        if (!isAdmin && !course.instructorId.equals(requesterId)) {
            throw new ForbiddenException("Anda tidak memiliki akses untuk mengubah kursus milik instruktur lain");
        }
    }
}
