package bitimage.messaging.beanstalk;

import java.nio.charset.StandardCharsets;

public class QueueMessage
{
    private final String message;

    protected QueueMessage(String message)
    {
        this.message = message;
    }

    public static QueueMessage CreateNew(String messageText)
    {
        return new QueueMessage(messageText);
    }

    public static QueueMessage CreateNew(byte[] messageBytes)
    {
        return new QueueMessage(new String(
                messageBytes,
                StandardCharsets.UTF_8));
    }

    public String toString()
    {
        return message;
    }

    public byte[] toBytes()
    {
        return message.getBytes();
    }

    public boolean isNull()
    {
        return false;
    }
}
