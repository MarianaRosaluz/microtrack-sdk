package org.microtrack.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.microtrack.dto.ResponseTrace;
import org.microtrack.dto.Trace;

import java.io.IOException;
import java.util.concurrent.Future;

public class CentralService {

    private final KafkaPublisher kafkaPublisher;
    private final ObjectMapper objectMapper;

    public CentralService() {
        this.kafkaPublisher = new KafkaPublisher();
        this.objectMapper = new ObjectMapper();
    }

    public CentralService(KafkaPublisher kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
        this.objectMapper = new ObjectMapper();
    }

    public ResponseTrace sendTrace(Trace trace) throws IOException {
        try {
            String requestBody = objectMapper.writeValueAsString(trace);

            System.out.println("TRACE: " + requestBody);

            String key = trace.getTraceId() != null ? trace.getTraceId() : String.valueOf(System.currentTimeMillis());
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

    public void close() {
        if (kafkaPublisher != null) {
            kafkaPublisher.close();
        }
    }

}
