package bitimage.domain.uploading.ports;

import bitimage.domain.common.events.IDomainEvent;

public interface IEventPublisher
{
    <T extends IDomainEvent> void publish(T event)
            throws Exception;
}
