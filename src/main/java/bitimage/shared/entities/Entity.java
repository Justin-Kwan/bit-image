package bitimage.shared.entities;

import java.time.Instant;

public abstract class Entity
{
    private final EntityID id;
    private final Instant dateTimeCreated;
    private final Instant dateTimeUpdated;

    protected Entity(EntityID id)
    {
        this.id = id;
        this.dateTimeCreated = Instant.now();
        this.dateTimeUpdated = Instant.now();
    }

    public EntityID getID()
    {
        return id;
    }

    public Instant getDateTimeCreated()
    {
        return dateTimeCreated;
    }

    public Instant getDateTimeUpdated()
    {
        return dateTimeUpdated;
    }
}
