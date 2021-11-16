package bitimage.messaging.beanstalk;

public interface MessageQueue
{
    void publish(QueueMessage message, String channelName);

    QueueMessage read(String channelName);
}
