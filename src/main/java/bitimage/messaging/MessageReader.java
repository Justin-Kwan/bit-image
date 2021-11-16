package bitimage.messaging;

import bitimage.eventhandling.EventHandler;
import bitimage.messaging.beanstalk.MessageQueue;
import bitimage.messaging.beanstalk.QueueMessage;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class MessageReader
{
    private final MessageQueue queue;
    private final Map<String, EventHandler> messageHandlers;
    private final ExecutorService workerPool;

    public MessageReader(
            MessageQueue queue,
            Map<String, EventHandler> messageHandlers,
            ExecutorService workerPool)
    {
        this.queue = queue;
        this.messageHandlers = Map.copyOf(messageHandlers);
        this.workerPool = workerPool;
    }

    /**
     * Continuously scans all tubes within queue,
     * polling for new messages for each tube.
     */
    public void readMessages()
    {
        while (true) {
            messageHandlers.forEach((queueTopic, messageHandler) -> {
                QueueMessage message = queue.read(queueTopic);

                if (message.isNull()) {
                    return;
                }

                dispatchMessage(message, messageHandler);
            });
        }
    }

    private void dispatchMessage(QueueMessage message, EventHandler messageHandler)
    {
        workerPool.submit(() -> {
            try {
                messageHandler.handle(message.toString());
            }
            catch (Exception e) {
                e.printStackTrace(System.out);
            }
        });
    }
}
