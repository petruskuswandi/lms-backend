-- Read-model lokal dari event catalog-service agar learning-service mandiri
CREATE TABLE course_snapshots (
    course_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    instructor_id UUID NOT NULL,
    price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    course_id UUID NOT NULL,
    course_title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PENDING_PAYMENT, COMPLETED
    progress_percentage INT NOT NULL DEFAULT 0,
    enrolled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)
);

CREATE TABLE lesson_progress (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_enrollment_lesson UNIQUE (enrollment_id, lesson_id)
);

CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_lesson_progress_enrollment ON lesson_progress(enrollment_id);