package bitimage.messaging.beanstalk;

import java.nio.charset.StandardCharsets;

public class QueueMessage {
  private final String message;

  protected QueueMessage(String message) {
    this.message = message;
  }

  public static QueueMessage CreateNew(String messageStr) {
    return new QueueMessage(messageStr);
  }

  public static QueueMessage CreateNew(byte[] messageBytes) {
    final var messageStr = new String(messageBytes, StandardCharsets.UTF_8);
    return new QueueMessage(messageStr);
  }

  public String toString() {
    return this.message;
  }

  public byte[] toBytes() {
    return this.message.getBytes();
  }

  public boolean isNull() {
    return false;
  }
}
