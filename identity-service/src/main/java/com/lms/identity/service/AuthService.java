package com.lms.identity.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lms.identity.dto.AuthResponse;
import com.lms.identity.entity.User;
import com.lms.identity.event.UserRegisteredEvent;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "lms.google.client-id")
    String googleClientId;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @Inject
    @Channel("identity-events")
    Emitter<UserRegisteredEvent> userEventEmitter;

    @Transactional
    public AuthResponse authenticateWithGoogle(String idTokenString) {
        GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);

        String googleSub = payload.getSubject();
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String avatarUrl = (String) payload.get("picture");

        User user = User.findByGoogleSub(googleSub);
        boolean isNewUser = false;

        if (user == null) {
            user = new User();
            user.googleSub = googleSub;
            user.email = email;
            user.fullName = (fullName != null) ? fullName : email.split("@")[0];
            user.avatarUrl = avatarUrl;
            user.role = "STUDENT";
            user.createdAt = OffsetDateTime.now();
            user.updatedAt = OffsetDateTime.now();
            user.persist();
            isNewUser = true;
        } else {
            user.fullName = (fullName != null) ? fullName : user.fullName;
            user.avatarUrl = avatarUrl;
            user.updatedAt = OffsetDateTime.now();
        }

        if (isNewUser) {
            UserRegisteredEvent event = new UserRegisteredEvent(
                user.id,
                user.email,
                user.fullName,
                user.role,
                OffsetDateTime.now()
            );
            userEventEmitter.send(event);
        }

        long expiresInSeconds = 86400L; // 24 jam
        String jwtToken = generateJwt(user, expiresInSeconds);

        return new AuthResponse(
            jwtToken,
            "Bearer",
            expiresInSeconds,
            new AuthResponse.UserProfile(
                user.id,
                user.email,
                user.fullName,
                user.avatarUrl,
                user.role
            )
        );
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
            )
            .setAudience(Collections.singletonList(googleClientId))
            .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new WebApplicationException("Token Google tidak valid atau sudah kedaluwarsa", Response.Status.UNAUTHORIZED);
            }
            return idToken.getPayload();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException("Gagal memverifikasi token Google: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    private String generateJwt(User user, long expiresInSeconds) {
        return Jwt.issuer(jwtIssuer)
            .subject(user.id.toString())
            .upn(user.email)
            .groups(Set.of(user.role))
            .claim("fullName", user.fullName)
            .expiresIn(Duration.ofSeconds(expiresInSeconds))
            .sign();
    }
}