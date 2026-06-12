package org.microtrack.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.microtrack.dto.LogEvent;
import org.microtrack.dto.ResponseTrace;
import org.microtrack.dto.Trace;

import java.io.IOException;
import java.util.concurrent.Future;

public class CentralService {

    private final KafkaPublisher kafkaPublisher;
    private final LogEventPublisher logEventPublisher;
    private final ObjectMapper objectMapper;

    public CentralService() {
        this.kafkaPublisher = new KafkaPublisher();
        this.logEventPublisher = new LogEventPublisher();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public CentralService(KafkaPublisher kafkaPublisher, LogEventPublisher logEventPublisher) {
        this.kafkaPublisher = kafkaPublisher;
        this.logEventPublisher = logEventPublisher;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public ResponseTrace sendTrace(Trace trace) throws IOException {
        try {
            String requestBody = objectMapper.writeValueAsString(trace);

            System.out.println("TRACE: " + requestBody);

            String key = trace.getTraceId() != null ? trace.getTraceId().toString() : String.valueOf(System.currentTimeMillis());
            Future<RecordMetadata> future = kafkaPublisher.publish(key, requestBody);

            RecordMetadata metadata = future.get();

            ResponseTrace responseTrace = new ResponseTrace();
            responseTrace.setStatusCode(200);
            responseTrace.setMessage("Trace publicado com sucesso no Kafka - Tópico: " + metadata.topic() + 
                    ", Partição: " + metadata.partition() + 
                    ", Offset: " + metadata.offset());

            return responseTrace;

        } catch (Exception exception) {
            System.out.print("Erro ao publicar trace no Kafka!");
            throw new IOException("Erro ao publicar trace no Kafka", exception);
        }
    }

    public ResponseTrace sendLog(LogEvent logEvent) throws IOException {
        try {
            String requestBody = objectMapper.writeValueAsString(logEvent);

            System.out.println("LOG EVENT: " + requestBody);

            String key = logEvent.getTraceId() != null ? logEvent.getTraceId().toString() : String.valueOf(System.currentTimeMillis());
            Future<RecordMetadata> future = logEventPublisher.publish(key, requestBody);

            RecordMetadata metadata = future.get();

            ResponseTrace responseTrace = new ResponseTrace();
            responseTrace.setStatusCode(200);
            responseTrace.setMessage("Log event publicado com sucesso no Kafka - Tópico: " + metadata.topic() + 
                    ", Partição: " + metadata.partition() + 
                    ", Offset: " + metadata.offset());

            return responseTrace;

        } catch (Exception exception) {
            System.out.print("Erro ao publicar log event no Kafka!");
            throw new IOException("Erro ao publicar log event no Kafka", exception);
        }
    }

    public void close() {
        if (kafkaPublisher != null) {
            kafkaPublisher.close();
        }
        if (logEventPublisher != null) {
            logEventPublisher.close();
        }
    }

}
