package bitimage.messaging.beanstalk;

import com.dinstone.beanstalkc.BeanstalkClientFactory;
import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.Job;
import com.dinstone.beanstalkc.JobConsumer;
import com.dinstone.beanstalkc.JobProducer;

public class BeanstalkMessageQueue
        implements MessageQueue
{
    private final BeanstalkClientFactory clientFactory;
    private final String queueName;

    public static final int RESERVE_JOB_TIMEOUT_MS = 1;
    public static final int PUBLISH_JOB_PRIORITY = 1;
    public static final int PUBLISH_JOB_DELAY = 0;
    public static final int PUBLISH_JOB_TTR = 5000;

    private BeanstalkMessageQueue(BeanstalkClientFactory clientFactory, String queueName)
    {
        this.clientFactory = clientFactory;
        this.queueName = queueName;
    }

    public static BeanstalkMessageQueue CreateNew(BeanstalkEnv env)
    {
        Configuration config = new Configuration();
        config.setServiceHost(env.getBeanstalkHost());
        config.setServicePort(env.getBeanstalkPort());
        config.setReadTimeout(env.getBeanstalkReadTimeout());
        config.setConnectTimeout(env.getBeanstalkConnectTimeout());

        return new BeanstalkMessageQueue(
                new BeanstalkClientFactory(config),
                env.getBeanstalkQueueName());
    }

    public void publish(QueueMessage message, String tubeName)
    {
        JobProducer producer = null;

        try {
            producer = clientFactory.createJobProducer(tubeName);
            producer.putJob(
                    PUBLISH_JOB_PRIORITY,
                    PUBLISH_JOB_DELAY,
                    PUBLISH_JOB_TTR,
                    message.toBytes());
        }
        finally {
            if (producer != null) {
                producer.close();
            }
        }
    }

    /**
     * Reads a new job from Beanstalk queue, blocking for RESERVE_JOB_TIMEOUT_MS defined in
     * milliseconds.
     *
     * <p>If not job is read within RESERVE_JOB_TIMEOUT_MS, then NullQueueMessage is returned.
     *
     * @param tubeName Name of tube (1 tube is user per job type).
     * @return String Job serialized as a json string.
     */
    public QueueMessage read(String tubeName)
    {
        JobConsumer consumer = null;

        try {
            consumer = clientFactory.createJobConsumer(tubeName);
            Job job = consumer.reserveJob(RESERVE_JOB_TIMEOUT_MS);

            if (job == null) {
                return new NullQueueMessage();
            }

            consumer.deleteJob(job.getId());
            return QueueMessage.CreateNew(job.getData());
        }
        finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }
}
