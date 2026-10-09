package com.eduaircontrol.msclassroom.infrastructure.messaging.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Contrato del evento de creacion de ambiente.
 *
 * <p>{@code of()} debe reflejar los datos REALES del ambiente: generaba UUIDs
 * aleatorios para {@code environmentId} y {@code code}, con lo que
 * ms-environment-monitoring replicaba filas que no correspondian a ningun
 * ambiente y jamas podia resolver su tipo para los umbrales.
 *
 * <p>La comprobacion de nombres de campo no es cosmética: el consumidor deserializa
 * contra {@code ClassroomEnvironmentEvent}, un record con esos mismos nombres. Si
 * aqui cambia una propiedad, el consumidor la ignora silenciosamente.
 */
class EducationalEnvironmentCreatedEventTest {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Test
    void ofCarriesTheRealEnvironmentData() {
        UUID environmentId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID environmentTypeId = UUID.randomUUID();

        EducationalEnvironmentCreatedEvent event = EducationalEnvironmentCreatedEvent.of(
                environmentId, "A-101", "Aula 101", campusId, environmentTypeId, 2, "ACTIVE",
                Instant.parse("2026-01-01T10:00:00Z"));

        assertThat(event.getPayload().getEnvironmentId()).isEqualTo(environmentId);
        assertThat(event.getPayload().getCode()).isEqualTo("A-101");
        assertThat(event.getPayload().getName()).isEqualTo("Aula 101");
        assertThat(event.getPayload().getCampusId()).isEqualTo(campusId);
        assertThat(event.getPayload().getEnvironmentTypeId()).isEqualTo(environmentTypeId);
        assertThat(event.getPayload().getFloor()).isEqualTo(2);
        assertThat(event.getPayload().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void aggregateIdIsTheEnvironmentItself() {
        UUID environmentId = UUID.randomUUID();

        EducationalEnvironmentCreatedEvent event = EducationalEnvironmentCreatedEvent.of(
                environmentId, "A-101", "Aula 101", UUID.randomUUID(), UUID.randomUUID(), 1,
                "ACTIVE", Instant.now());

        assertThat(event.getAggregateId()).isEqualTo(environmentId);
        assertThat(event.getAggregateType()).isEqualTo("EducationalEnvironment");
    }

    @Test
    void routingKeyIsTheOneTheConsumerBindsTo() {
        // ms-environment-monitoring se suscribe a classroom.educational_environment.*
        assertThat(EducationalEnvironmentCreatedEvent.ROUTING_KEY)
                .startsWith("classroom.educational_environment.");
    }

    @Test
    void serializedEnvelopeMatchesWhatTheConsumerReads() throws Exception {
        UUID environmentId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID environmentTypeId = UUID.randomUUID();

        EducationalEnvironmentCreatedEvent event = EducationalEnvironmentCreatedEvent.of(
                environmentId, "A-101", "Aula 101", campusId, environmentTypeId, 2, "ACTIVE",
                Instant.parse("2026-01-01T10:00:00Z"));

        String json = MAPPER.writeValueAsString(event);

        assertThat(json)
                .contains("\"eventType\":\"" + EducationalEnvironmentCreatedEvent.TYPE + "\"")
                .contains("\"environmentId\":\"" + environmentId + "\"")
                .contains("\"code\":\"A-101\"")
                .contains("\"name\":\"Aula 101\"")
                .contains("\"campusId\":\"" + campusId + "\"")
                .contains("\"environmentTypeId\":\"" + environmentTypeId + "\"")
                .contains("\"floor\":2")
                .contains("\"status\":\"ACTIVE\"");
    }

    @Test
    void eventIdIsUniquePerEvent() {
        UUID environmentId = UUID.randomUUID();

        UUID first = EducationalEnvironmentCreatedEvent.of(
                environmentId, "A-101", "Aula 101", UUID.randomUUID(), UUID.randomUUID(), 1,
                "ACTIVE", Instant.now()).getEventId();
        UUID second = EducationalEnvironmentCreatedEvent.of(
                environmentId, "A-101", "Aula 101", UUID.randomUUID(), UUID.randomUUID(), 1,
                "ACTIVE", Instant.now()).getEventId();

        // El consumidor deduplica por eventId: dos eventos del mismo ambiente deben
        // poder procesarse los dos.
        assertThat(first).isNotEqualTo(second);
    }
}
