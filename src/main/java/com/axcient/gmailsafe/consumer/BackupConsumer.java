package com.axcient.gmailsafe.consumer;

import com.axcient.gmailsafe.entity.Email;
import com.axcient.gmailsafe.service.BackupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class BackupConsumer {

    private final BackupService backupService;

    @RabbitListener(queues = {"${rabbitmq.queues.gmail_safe}"})
    public void consumer(Email email) {
        backupService.saveEmail(email);
    }

}
