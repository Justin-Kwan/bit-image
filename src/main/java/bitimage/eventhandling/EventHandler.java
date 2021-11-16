package bitimage.eventhandling;

public interface EventHandler
{
    void handle(String message)
            throws Exception;
}
