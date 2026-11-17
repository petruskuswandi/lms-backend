package com.lms.identity.resource;

import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.lms.identity.dto.AuthResponse;
import com.lms.identity.dto.GoogleLoginRequest;
import com.lms.identity.entity.User;
import com.lms.identity.service.AuthService;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/google")
    @PermitAll
    public Response loginWithGoogle(@Valid GoogleLoginRequest request) {
        AuthResponse response = authService.authenticateWithGoogle(request.idToken());
        return Response.ok(response).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"STUDENT", "INSTRUCTOR", "ADMIN"})
    public Response getCurrentUser() {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = User.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new AuthResponse.UserProfile(
            user.id,
            user.email,
            user.fullName,
            user.avatarUrl,
            user.role
        )).build();
    }
}