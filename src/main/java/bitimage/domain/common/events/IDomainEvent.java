package bitimage.domain.common.events;

import java.time.Instant;

public interface IDomainEvent
{
    Instant getTimeOccurred();
}
