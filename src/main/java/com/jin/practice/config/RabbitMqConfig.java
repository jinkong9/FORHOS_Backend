package com.jin.practice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String RECEPTION_EXCHANGE = "forhos.reception.exchange";
    public static final String RECEPTION_CREATE_QUEUE = "forhos.reception.create.queue";
    public static final String RECEPTION_CREATE_ROUTING_KEY = "reception.create";

    @Bean
    public DirectExchange receptionExchange() {
        return new DirectExchange(RECEPTION_EXCHANGE);
    }

    @Bean
    public Queue receptionCreateQueue() {
        return QueueBuilder.durable(RECEPTION_CREATE_QUEUE).build();
    }

    @Bean
    public Binding receptionCreateBinding(
            Queue receptionCreateQueue,
            DirectExchange receptionExchange
    ) {
        return BindingBuilder
                .bind(receptionCreateQueue)
                .to(receptionExchange)
                .with(RECEPTION_CREATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        return factory;
    }
}
