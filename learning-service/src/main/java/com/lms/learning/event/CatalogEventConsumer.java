package com.lms.learning.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.learning.entity.CourseSnapshot;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CatalogEventConsumer {

    private static final Logger LOG = Logger.getLogger(CatalogEventConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Incoming("catalog-events-in")
    @Blocking
    @Transactional
    public void onCatalogEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            UUID courseId = UUID.fromString(node.get("courseId").asText());
            String title = node.get("title").asText();
            UUID instructorId = UUID.fromString(node.get("instructorId").asText());
            BigDecimal price = node.has("price") ? new BigDecimal(node.get("price").asText()) : BigDecimal.ZERO;
            String status = node.get("status").asText();
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "COURSE_UPDATED";

            if ("COURSE_DELETED".equals(eventType)) {
                CourseSnapshot.deleteById(courseId);
                LOG.infof("CourseSnapshot dihapus: %s (%s)", title, courseId);
                return;
            }

            int totalLessons = node.has("totalLessons") ? node.get("totalLessons").asInt() : 0;

            CourseSnapshot snapshot = CourseSnapshot.findById(courseId);
            if (snapshot == null) {
                snapshot = new CourseSnapshot();
                snapshot.courseId = courseId;
            }
            snapshot.title = title;
            snapshot.instructorId = instructorId;
            snapshot.price = price;
            snapshot.status = status;
            snapshot.totalLessons = totalLessons;
            snapshot.updatedAt = OffsetDateTime.now();
            snapshot.persist();

            LOG.infof("Sinkronisasi CourseSnapshot berhasil: %s (%s) -> status=%s", title, courseId, status);
        } catch (Exception e) {
            LOG.errorf(e, "Gagal memproses event dari lms.catalog.events: %s", payload);
        }
    }
}