package com.football_club.Scouting.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SEASONAL_REQUEST_QUEUE = "seasonal-report-request-queue";
    public static final String SEASONAL_RESPONSE_QUEUE = "seasonal-report-response-queue";
    public static final String EXCHANGE = "scouting-exchange";
    public static final String ROUTING_KEY_REQUEST = "seasonal.request";
    public static final String ROUTING_KEY_RESPONSE = "seasonal.response";

    @Bean
    public Queue requestQueue() {
        return new Queue(SEASONAL_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(SEASONAL_RESPONSE_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding requestBinding(Queue requestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(requestQueue).to(exchange).with(ROUTING_KEY_REQUEST);
    }

    @Bean
    public Binding responseBinding(Queue responseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(responseQueue).to(exchange).with(ROUTING_KEY_RESPONSE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}