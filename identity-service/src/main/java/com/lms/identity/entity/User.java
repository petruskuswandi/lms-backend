package com.lms.identity.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(name = "avatar_url")
    public String avatarUrl;

    @Column(name = "google_sub", nullable = false, unique = true)
    public String googleSub;

    @Column(nullable = false)
    public String role = "STUDENT";

    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static User findByGoogleSub(String googleSub) {
        return find("googleSub", googleSub).firstResult();
    }
}