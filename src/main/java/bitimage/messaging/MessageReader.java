package bitimage.messaging;

import bitimage.eventhandling.IEventHandler;
import bitimage.messaging.beanstalk.IMessageQueue;
import bitimage.messaging.beanstalk.QueueMessage;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class MessageReader
{
    private final IMessageQueue queue;
    private final Map<String, IEventHandler> messageHandlers;
    private final ExecutorService workerPool;

    public MessageReader(
            IMessageQueue queue,
            Map<String, IEventHandler> messageHandlers,
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

    private void dispatchMessage(QueueMessage message, IEventHandler messageHandler)
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
