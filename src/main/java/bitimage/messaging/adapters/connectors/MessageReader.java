package bitimage.messaging.adapters.connectors;

import bitimage.events.IEventHandler;
import bitimage.messaging.beanstalk.IMessageQueue;
import bitimage.messaging.beanstalk.QueueMessage;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class MessageReader {

  private final IMessageQueue queue;
  private final Map<String, IEventHandler> messageHandlers;
  private final ExecutorService workerPool;

  public MessageReader(
      IMessageQueue queue, Map<String, IEventHandler> messageHandlers, ExecutorService workerPool) {
    this.queue = queue;
    this.messageHandlers = messageHandlers;
    this.workerPool = workerPool;
  }

  /** Continuously scans all tubes within queue, polling for new messages for each tube. */
  public void readMessages() {
    while (true) {
      this.messageHandlers.forEach((queueTopic, messageHandler) -> {
        this.listenForMessage(queueTopic);
      });
    }
  }

  /**
   * Attempts to read new message from queue tube. When a new message is read, the message handler
   * corresponding to the queue name is executed by the worker thread pool.
   */
  private void listenForMessage(String queueTopic) {
    QueueMessage message = this.queue.read(queueTopic);

    if (message.isNull()) {
      return;
    }

    final IEventHandler messageHandler = this.messageHandlers.get(queueTopic);

    this.workerPool.submit(() -> {
      try {
        messageHandler.handle(message.toString());
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }
    });
  }
}
