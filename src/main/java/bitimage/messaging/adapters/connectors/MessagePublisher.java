package bitimage.messaging.adapters.connectors;

import bitimage.domain.common.events.IDomainEvent;
import bitimage.domain.uploading.ports.IEventPublisher;
import bitimage.messaging.beanstalk.IMessageQueue;
import bitimage.messaging.beanstalk.QueueMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessagePublisher implements IEventPublisher {

  private final IMessageQueue queue;
  private final Map<String, String> eventToQueueLookup;
  private final Logger logger;

  public MessagePublisher(IMessageQueue queue, Map<String, String> eventToQueueLookup) {
    this.queue = queue;
    this.eventToQueueLookup = eventToQueueLookup;
    this.logger = Logger.getLogger("Message publisher logger");
  }

  /**
   * Publishes a domain event asynchronously to job queue for further entity processing.
   *
   * <p>ex. Newly uploaded images' data is serialized and then sent to message queue for image
   * analysis.
   */
  public <T extends IDomainEvent> void publish(T event) throws Exception {
    ObjectWriter jsonWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    final String messageStr = jsonWriter.writeValueAsString(event);
    final QueueMessage message = QueueMessage.CreateNew(messageStr);

    final String eventName = event.getClass().getSimpleName();
    final String channelName = this.eventToQueueLookup.get(eventName);

    CompletableFuture.runAsync(() -> this.queue.publish(message, channelName));

    final String logMessage = "Message published queue channel '%s'".formatted(channelName);
    this.logger.log(Level.INFO, logMessage);
  }
}
