package com.example.personalbrain.ingestion.queue.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.personalbrain.ingestion.queue.dto.EmbeddingJob;


@Component
public class EmbeddingPublisher {
    private final RabbitTemplate rabbit;
    EmbeddingPublisher(@Qualifier("rabbitTemplateEmbed") RabbitTemplate rabbitTemplate) {
        this.rabbit = rabbitTemplate;
    }

    public void publish(EmbeddingJob job) {
        rabbit.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbit.convertAndSend("embedding.exchange", "embedding.route", job);
    }
}