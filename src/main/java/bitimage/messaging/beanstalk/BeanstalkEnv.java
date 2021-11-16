package bitimage.messaging.beanstalk;

public interface BeanstalkEnv
{
    String getBeanstalkHost();

    String getBeanstalkQueueName();

    int getBeanstalkPort();

    int getBeanstalkReadTimeout();

    int getBeanstalkConnectTimeout();
}
