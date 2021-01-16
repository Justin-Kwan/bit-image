package bitimage.messaging.beanstalk;

public interface IBeanstalkEnv {
  public String getBeanstalkHost();

  public String getBeanstalkQueueName();

  public int getBeanstalkPort();

  public int getBeanstalkReadTimeout();

  public int getBeanstalkConnectTimeout();
}
