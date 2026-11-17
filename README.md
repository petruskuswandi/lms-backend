# LMS Microservices Backend (Quarkus + Kafka + PostgreSQL)

Arsitektur microservices Learning Management System menggunakan Java 17+, Quarkus, PostgreSQL, dan Apache Kafka.

## Struktur Layanan
- **identity-service (:8081)** — Google OAuth2 login, penerbitan JWT RSA256, manajemen role (ADMIN, INSTRUCTOR, STUDENT), dan producer event lms.identity.events.
- **catalog-service (:8082)** — Manajemen kursus, modul, dan materi (Course, Module, Lesson) dengan RBAC & producer event lms.catalog.events.
- **learning-service (:8083)** — Pendaftaran kursus (*enrollment*), pelacakan progres belajar, consumer event lms.catalog.events, dan producer lms.learning.events.
- **infra/** — Docker Compose untuk PostgreSQL (5433), Apache Kafka (9092), dan Kafbat UI (8090).

## Cara Menjalankan
1. Jalankan infrastruktur Docker:
   \\\ash
   cd infra && docker compose up -d
   \\\
2. Buat kunci RSA (privateKey.pem & publicKey.pem) di identity-service/src/main/resources/ dan salin publicKey.pem ke catalog-service serta learning-service.
3. Jalankan masing-masing service menggunakan ./mvnw quarkus:dev.
