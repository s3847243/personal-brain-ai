package com.example.personalbrain.ingestion.queue.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.personalbrain.ingestion.queue.dto.IngestionJob;

@Component
public class JobPublisher {
    private final RabbitTemplate rabbit;
    
    JobPublisher(@Qualifier("rabbitTemplate") RabbitTemplate rabbit){ this.rabbit=rabbit; }
    
    public void publish(IngestionJob job){
        rabbit.setMessageConverter(new Jackson2JsonMessageConverter());

        rabbit.convertAndSend("ingest.exchange", "ingest.route", job);
    }
}