package com.axcient.gmailsafe.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.exchanges.gmail_safe}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.gmail_safe}")
    private String routingKey;

    @Value("${rabbitmq.queues.gmail_safe}")
    private String queue;

    @Bean
    Queue createQueue() {
        return new Queue(queue, true);
    }

    @Bean
    DirectExchange createExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    Binding createBinding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

}
