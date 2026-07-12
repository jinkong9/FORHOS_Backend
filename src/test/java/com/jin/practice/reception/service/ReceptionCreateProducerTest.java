package com.jin.practice.reception.service;

import com.jin.practice.config.RabbitMqConfig;
import com.jin.practice.reception.dto.ReceptionCreateMessage;
import com.jin.practice.reception.entity.VisitType;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReceptionCreateProducerTest {

    @Test
    void publishSendsMessageToReceptionExchange() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        ReceptionCreateProducer producer = new ReceptionCreateProducer(rabbitTemplate);
        ReceptionCreateMessage message = new ReceptionCreateMessage(
                "request-id",
                "user@example.com",
                1L,
                "Patient",
                VisitType.FIRST,
                "Headache",
                LocalDateTime.now()
        );

        producer.publish(message);

        verify(rabbitTemplate).convertAndSend(
                RabbitMqConfig.RECEPTION_EXCHANGE,
                RabbitMqConfig.RECEPTION_CREATE_ROUTING_KEY,
                message
        );
    }
}
