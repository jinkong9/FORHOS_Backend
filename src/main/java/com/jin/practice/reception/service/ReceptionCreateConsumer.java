package com.jin.practice.reception.service;

import com.jin.practice.config.RabbitMqConfig;
import com.jin.practice.reception.dto.ReceptionCreateMessage;
import com.jin.practice.reception.dto.ReceptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceptionCreateConsumer {
    private final ReceptionService receptionService;

    @RabbitListener(queues = RabbitMqConfig.RECEPTION_CREATE_QUEUE)
    public void consume(ReceptionCreateMessage message) {
        try {
            log.info("비동기 접수 메시지 수신. requestId={}, email={}, hospitalId={}",
                    message.requestId(),
                    message.email(),
                    message.hospitalId()
            );

            ReceptionDto reception = receptionService.createReception(
                    message.email(),
                    message.toCreateDto()
            );

            log.info("비동기 접수 처리 완료. requestId={}, receptionId={}",
                    message.requestId(),
                    reception.id()
            );
        } catch (RuntimeException exception) {
            log.warn("비동기 접수 처리 실패. requestId={}", message.requestId(), exception);
        }
    }
}
