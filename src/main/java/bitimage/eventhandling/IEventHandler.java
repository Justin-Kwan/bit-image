package bitimage.eventhandling;

public interface IEventHandler
{
    void handle(String message)
            throws Exception;
}
