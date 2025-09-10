package com.example.personalbrain.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingQueueConfig {

    @Bean
    public Queue embeddingQueue() {
        return new Queue("embedding.queue", true);
    }

    @Bean
    public DirectExchange embeddingExchange() {
        return new DirectExchange("embedding.exchange");
    }

    @Bean
    public Binding embeddingBinding(Queue embeddingQueue, DirectExchange embeddingExchange) {
        return BindingBuilder.bind(embeddingQueue).to(embeddingExchange).with("embedding.route");
    }

    @Bean
    public MessageConverter jsonMessageConverterEmbed() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactoryEmbed(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverterJob
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverterJob);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplateEmbed(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverterJob
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverterJob);
        return template;
    }
}