package bitimage.uploading.ports;

import bitimage.shared.events.DomainEvent;

public interface EventPublisher
{
    <T extends DomainEvent> void publish(T event)
            throws Exception;
}
