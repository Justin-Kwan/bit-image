package bitimage.messaging.adapters.connectors;

import bitimage.events.IEventHandler;
import bitimage.messaging.beanstalk.BeanstalkMessageQueue;
import bitimage.messaging.beanstalk.QueueMessage;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class MessageReader {

  private final BeanstalkMessageQueue queue;
  private final Map<String, IEventHandler> messageHandlers;
  private final ExecutorService workerPool;

  public MessageReader(
      BeanstalkMessageQueue queue,
      Map<String, IEventHandler> messageHandlers,
      ExecutorService workerPool) {
    this.queue = queue;
    this.messageHandlers = messageHandlers;
    this.workerPool = workerPool;
  }

  /** Continuously scans all topics within queue, polling for new messages for each topic. */
  public void readMessages() {
    while (true) {
      this.messageHandlers.forEach(
          (queueTopic, messageHandler) -> {
            this.listenForMessage(queueTopic);
          });
    }
  }

  /**
   * Attempts to read new message from queue topic. When a new message is read, the message handler
   * corresponding to the queue name is executed by the worker thread pool.
   */
  private void listenForMessage(String queueTopic) {
    QueueMessage message = this.queue.read(queueTopic);

    if (message.isNull()) {
      System.out.println("No messages for queue : " + queueTopic);
      return;
    }

    System.out.println(
        "Message dequed for queue : " + queueTopic + " message : " + message.toString());
    final IEventHandler messageHandler = this.messageHandlers.get(queueTopic);

    this.workerPool.submit(
        () -> {
          try {
            messageHandler.handle(message.toString());
          } catch (Exception e) {
            e.printStackTrace(System.out);
          }
        });
  }
}
