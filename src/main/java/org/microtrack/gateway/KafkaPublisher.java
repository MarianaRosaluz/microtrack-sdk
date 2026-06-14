package org.microtrack.gateway;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaPublisher {

    private static volatile KafkaPublisher instance;
    private final KafkaProducer<String, String> producer;
    private final String topicName;

    private KafkaPublisher() {
        Properties props = loadKafkaProperties();
        this.topicName = props.getProperty("topic.name", "trace-events");
        this.producer = new KafkaProducer<>(props);
    }

    private KafkaPublisher(Properties customProps) {
        this.topicName = customProps.getProperty("topic.name", "trace-events");
        this.producer = new KafkaProducer<>(customProps);
    }

    public static KafkaPublisher getInstance() {
        if (instance == null) {
            synchronized (KafkaPublisher.class) {
                if (instance == null) {
                    instance = new KafkaPublisher();
                }
            }
        }
        return instance;
    }

    public static KafkaPublisher getInstance(Properties customProps) {
        if (instance == null) {
            synchronized (KafkaPublisher.class) {
                if (instance == null) {
                    instance = new KafkaPublisher(customProps);
                }
            }
        }
        return instance;
    }

    private Properties loadKafkaProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("kafka.properties")) {
            if (input == null) {
                throw new RuntimeException("Arquivo kafka.properties não encontrado!");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar configurações do Kafka", e);
        }
        return props;
    }

    public Future<RecordMetadata> publish(String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, message);
        return producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("Erro ao publicar mensagem no Kafka: " + exception.getMessage());
                exception.printStackTrace();
            } else {
                System.out.println("Mensagem publicada com sucesso no tópico: " + metadata.topic() +
                        ", partição: " + metadata.partition() +
                        ", offset: " + metadata.offset());
            }
        });
    }

    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
    }
}
