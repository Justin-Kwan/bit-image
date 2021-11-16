package bitimage.messaging.beanstalk;

public interface IMessageQueue
{
    void publish(QueueMessage message, String channelName);

    QueueMessage read(String channelName);
}
