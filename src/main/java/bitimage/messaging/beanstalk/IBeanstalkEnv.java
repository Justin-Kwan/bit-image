package bitimage.messaging.beanstalk;

public interface IBeanstalkEnv
{
    String getBeanstalkHost();

    String getBeanstalkQueueName();

    int getBeanstalkPort();

    int getBeanstalkReadTimeout();

    int getBeanstalkConnectTimeout();
}
