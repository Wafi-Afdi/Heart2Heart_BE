package com.heart2heart.be_app.config;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String ECG_EXCHANGE_NAME = "ecg_exchange";
    public static final String ECG_QUEUE_NAME = "ecg_signals_queue";
    public static final String ECG_ROUTING_KEY = "ecg.signals.new";

    public static final String BPM_EXCHANGE_NAME = "bpm_exchange";
    public static final String BPM_QUEUE_NAME = "bpm_data_queue";
    public static final String BPM_ROUTING_KEY = "bpm.data.new";

    public static final String REPORT_EXCHANGE_NAME = "report_exchange";
    public static final String REPORT_QUEUE_NAME = "report_data_queue";
    public static final String REPORT_ROUTING_KEY = "report.data.new";

    @Bean
    public TopicExchange ecgExchange() {
        return new TopicExchange(ECG_EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange bpmExchange() {
        return new TopicExchange(BPM_EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange reportExchange() {
        return new TopicExchange(REPORT_EXCHANGE_NAME);
    }

    @Bean
    public Queue ecgSignalsQueue() {
        return new Queue(ECG_QUEUE_NAME, true); // durable=true
    }

    @Bean
    public Queue bpmDataQueue() {
        return new Queue(BPM_QUEUE_NAME, true); // durable=true
    }

    @Bean
    public Queue reportDataQueue() {
        return new Queue(REPORT_QUEUE_NAME, true); // durable=true
    }

    @Bean
    public Binding bindingECG(Queue ecgSignalsQueue, TopicExchange ecgExchange) {
        return BindingBuilder.bind(ecgSignalsQueue)
                .to(ecgExchange)
                .with(ECG_ROUTING_KEY);
    }

    @Bean
    public Binding bindingBPM(Queue bpmDataQueue, TopicExchange bpmExchange) {
        return BindingBuilder.bind(bpmDataQueue)
                .to(bpmExchange)
                .with(BPM_ROUTING_KEY);
    }

    @Bean
    public Binding bindingReport(Queue reportDataQueue, TopicExchange reportExchange) {
        return BindingBuilder.bind(reportDataQueue)
                .to(reportExchange)
                .with(REPORT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }


}
