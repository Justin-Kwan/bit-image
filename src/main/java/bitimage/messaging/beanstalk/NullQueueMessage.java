package bitimage.messaging.beanstalk;

public class NullQueueMessage
        extends QueueMessage
{
    public NullQueueMessage()
    {
        super(null);
    }

    @Override
    public boolean isNull()
    {
        return true;
    }
}
