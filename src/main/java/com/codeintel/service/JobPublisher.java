package main.java.com.codeintel.service;

import com.codeintel.config.RabbitMQConfig;
import com.codeintel.model.FileJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public JobPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public int publishJobs(List<FileJob> jobs) {
        int published = 0;

        for (FileJob job : jobs) {
            try {
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    job
                );
                published++;
            } catch (Exception e) {
                log.error("Failed to publish job for file {}: {}",
                    job.getFilePath(), e.getMessage());
            }
        }

        log.info("Published {}/{} jobs to queue", published, jobs.size());
        return published;
    }
}
