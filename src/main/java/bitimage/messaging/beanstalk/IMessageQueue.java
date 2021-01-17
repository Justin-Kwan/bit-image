package bitimage.messaging.beanstalk;

public interface IMessageQueue {
  public void publish(QueueMessage message, String channelName);

  public QueueMessage read(String channelName);
}
