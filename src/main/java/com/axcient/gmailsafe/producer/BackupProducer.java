package com.axcient.gmailsafe.producer;

import com.axcient.gmailsafe.entity.Email;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class BackupProducer {

    private final AmqpTemplate amqpTemplate;

    public void publisher(Email payload, String exchange, String routingKey) {
        amqpTemplate.convertAndSend(exchange, routingKey, payload);
    }

}
