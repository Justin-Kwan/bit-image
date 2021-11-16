package bitimage.messaging;

import bitimage.shared.events.DomainEvent;
import bitimage.uploading.ports.EventPublisher;
import bitimage.messaging.beanstalk.MessageQueue;
import bitimage.messaging.beanstalk.QueueMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessagePublisher
        implements EventPublisher
{
    private final MessageQueue queue;
    private final Map<String, String> eventToQueueLookup;
    private final Logger logger;

    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .writer()
            .withDefaultPrettyPrinter();

    public MessagePublisher(MessageQueue queue, Map<String, String> eventToQueueLookup)
    {
        this.queue = queue;
        this.eventToQueueLookup = Map.copyOf(eventToQueueLookup);
        this.logger = Logger.getLogger("Message publisher logger");
    }

    /**
     * Publishes a domain event asynchronously to job queue
     * for further entity processing.
     * <p>
     * ex. Newly uploaded images' data is serialized and
     * then sent to message queue for image analysis.
     */
    public <T extends DomainEvent> void publish(T event)
            throws Exception
    {
        String messageText = JSON_WRITER.writeValueAsString(event);
        QueueMessage message = QueueMessage.CreateNew(messageText);

        String eventName = event.getClass().getSimpleName();
        String channelName = eventToQueueLookup.get(eventName);

        CompletableFuture.runAsync(() -> queue.publish(message, channelName));

        logger.log(
                Level.INFO,
                String.format("Message published queue channel '%s'", channelName));
    }
}
