package bitimage.shared.events;

import java.time.Instant;

public interface DomainEvent
{
    Instant getTimeOccurred();
}
