package com.lms.learning.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.lms.learning.dto.CompleteLessonRequest;
import com.lms.learning.dto.EnrollRequest;
import com.lms.learning.entity.CourseSnapshot;
import com.lms.learning.entity.Enrollment;
import com.lms.learning.service.EnrollmentService;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/enrollments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnrollmentResource {

    @Inject
    EnrollmentService enrollmentService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/synced-courses")
    @PermitAll
    public List<CourseSnapshot> getSyncedCourses() {
        return enrollmentService.listAvailableCourses();
    }

    @GET
    @Path("/my-courses")
    @RolesAllowed({"STUDENT", "INSTRUCTOR", "ADMIN"})
    public List<Enrollment> getMyCourses() {
        UUID studentId = UUID.fromString(jwt.getSubject());
        return enrollmentService.getMyEnrollments(studentId);
    }

    @POST
    @RolesAllowed({"STUDENT", "INSTRUCTOR", "ADMIN"})
    public Response enrollCourse(@Valid EnrollRequest request) {
        UUID studentId = UUID.fromString(jwt.getSubject());
        String studentName = jwt.getClaim("name");
        if (studentName == null) {
            studentName = jwt.getName();
        }
        Enrollment created = enrollmentService.enroll(request.courseId(), studentId, studentName);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/{courseId}/progress")
    @RolesAllowed({"STUDENT", "INSTRUCTOR", "ADMIN"})
    public Enrollment completeLesson(
            @PathParam("courseId") UUID courseId,
            @Valid CompleteLessonRequest request) {
        UUID studentId = UUID.fromString(jwt.getSubject());
        return enrollmentService.markLessonCompleted(courseId, studentId, request);
    }
}