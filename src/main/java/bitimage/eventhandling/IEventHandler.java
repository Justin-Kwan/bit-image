package bitimage.eventhandling;

public interface IEventHandler {
  public void handle(String message) throws Exception;
}
