package main.java.com.codeintel.listener;

import com.codeintel.config.RabbitMQConfig;
import com.codeintel.model.FileJob;
import com.codeintel.model.ParsedFile;
import com.codeintel.service.FileProcessingService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FileJobListener {

    private static final Logger log = LoggerFactory.getLogger(FileJobListener.class);

    private final FileProcessingService fileProcessingService;

    public FileJobListener(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE, ackMode = "MANUAL")
    public void handleFileJob(FileJob job,
                               Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws Exception {
        try {
            log.info("Processing job: {} [{}]", job.getFilePath(), job.getLanguage());

            // the repo root path would come from a shared temp directory or a configuration property
            // now just job metadata
            Path repoRoot = Path.of(System.getProperty("java.io.tmpdir"),
                "codeintel-" + job.getRepoName().replace("/", "-"));

            ParsedFile parsed = fileProcessingService.process(job, repoRoot);

            log.info("Completed: {} — {} symbols extracted",
                parsed.getFilePath(), parsed.getSymbolCount());

            // index and store parsed logic:
            // searchIndexService.index(parsed);
            // metadataStorageService.store(parsed);

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to process {}: {}", job.getFilePath(), e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}