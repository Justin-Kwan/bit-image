package bitimage.messaging.adapters.connectors;

import bitimage.domain.common.events.IDomainEvent;
import bitimage.domain.uploading.ports.IEventPublisher;
import bitimage.messaging.beanstalk.BeanstalkMessageQueue;
import bitimage.messaging.beanstalk.QueueMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MessagePublisher implements IEventPublisher {

  private final BeanstalkMessageQueue queue;
  private final Map<String, String> eventToQueueLookup;

  public MessagePublisher(BeanstalkMessageQueue queue, Map<String, String> eventToQueueLookup) {
    this.queue = queue;
    this.eventToQueueLookup = eventToQueueLookup;
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
    final String queueName = this.eventToQueueLookup.get(eventName);

    System.out.println(
        "Publishing to queue name: :::::: " + queueName + " with event name ::: " + eventName);

    CompletableFuture.runAsync(() -> this.queue.publish(message, queueName));

    final String logMessage = "Message published '%s'".formatted(messageStr);
    System.out.println(logMessage);
  }
}
