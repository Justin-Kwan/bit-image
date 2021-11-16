package bitimage.uploading.events;

import bitimage.shared.entities.EntityID;
import bitimage.shared.events.DomainEvent;

import java.time.Instant;

public class UserDeletedEvent
        implements DomainEvent
{
    private final EntityID userID;
    private final Instant timeOccurred;

    public UserDeletedEvent(EntityID userID)
    {
        this.userID = userID;
        this.timeOccurred = Instant.now();
    }

    public EntityID getUserID()
    {
        return userID;
    }

    public Instant getTimeOccurred()
    {
        return timeOccurred;
    }
}
