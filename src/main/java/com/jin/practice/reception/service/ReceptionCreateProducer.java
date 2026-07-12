package com.jin.practice.reception.service;

import com.jin.practice.config.RabbitMqConfig;
import com.jin.practice.reception.dto.ReceptionCreateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceptionCreateProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publish(ReceptionCreateMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.RECEPTION_EXCHANGE,
                RabbitMqConfig.RECEPTION_CREATE_ROUTING_KEY,
                message
        );
    }
}
