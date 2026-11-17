package com.lms.catalog.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.lms.catalog.dto.CourseRequest;
import com.lms.catalog.dto.LessonRequest;
import com.lms.catalog.dto.ModuleRequest;
import com.lms.catalog.entity.Course;
import com.lms.catalog.entity.CourseModule;
import com.lms.catalog.entity.Lesson;
import com.lms.catalog.service.CourseService;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CourseResource {

    @Inject
    CourseService courseService;

    @Inject
    JsonWebToken jwt;

    @GET
    @PermitAll
    public List<Course> getAllCourses() {
        return courseService.listAllCourses();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Course getCourse(@PathParam("id") UUID id) {
        return courseService.getCourseById(id);
    }

    @POST
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Response createCourse(@Valid CourseRequest request) {
        UUID instructorId = UUID.fromString(jwt.getSubject());
        String instructorName = jwt.getClaim("name");
        if (instructorName == null) {
            instructorName = jwt.getName();
        }
        Course created = courseService.createCourse(request, instructorId, instructorName);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Course updateCourse(@PathParam("id") UUID id, @Valid CourseRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        return courseService.updateCourse(id, request, requesterId, isAdmin);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Response deleteCourse(@PathParam("id") UUID id) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        courseService.deleteCourse(id, requesterId, isAdmin);
        return Response.noContent().build();
    }

    @POST
    @Path("/{courseId}/modules")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Response addModule(@PathParam("courseId") UUID courseId, @Valid ModuleRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        CourseModule created = courseService.addModule(courseId, request, requesterId, isAdmin);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{courseId}/modules/{moduleId}")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public CourseModule updateModule(
            @PathParam("courseId") UUID courseId,
            @PathParam("moduleId") UUID moduleId,
            @Valid ModuleRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        return courseService.updateModule(courseId, moduleId, request, requesterId, isAdmin);
    }

    @DELETE
    @Path("/{courseId}/modules/{moduleId}")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Response deleteModule(
            @PathParam("courseId") UUID courseId,
            @PathParam("moduleId") UUID moduleId) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        courseService.deleteModule(courseId, moduleId, requesterId, isAdmin);
        return Response.noContent().build();
    }

    @POST
    @Path("/{courseId}/modules/{moduleId}/lessons")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Response addLesson(
            @PathParam("courseId") UUID courseId,
            @PathParam("moduleId") UUID moduleId,
            @Valid LessonRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        Lesson created = courseService.addLesson(courseId, moduleId, request, requesterId, isAdmin);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Lesson updateLesson(
            @PathParam("courseId") UUID courseId,
            @PathParam("moduleId") UUID moduleId,
            @PathParam("lessonId") UUID lessonId,
            @Valid LessonRequest request) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        return courseService.updateLesson(courseId, moduleId, lessonId, request, requesterId, isAdmin);
    }

    @DELETE
    @Path("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    @RolesAllowed({"INSTRUCTOR", "ADMIN"})
    public Response deleteLesson(
            @PathParam("courseId") UUID courseId,
            @PathParam("moduleId") UUID moduleId,
            @PathParam("lessonId") UUID lessonId) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        courseService.deleteLesson(courseId, moduleId, lessonId, requesterId, isAdmin);
        return Response.noContent().build();
    }
}
