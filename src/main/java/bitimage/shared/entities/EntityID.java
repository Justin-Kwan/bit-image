package bitimage.shared.entities;

import java.util.UUID;

public class EntityID
{
    public final UUID id;

    private EntityID(UUID id)
    {
        this.id = id;
    }

    public static EntityID CreateNew()
    {
        return new EntityID(UUID.randomUUID());
    }

    public static EntityID CreateNew(String id)
    {
        return new EntityID(UUID.fromString(id));
    }

    public static EntityID CreateNew(UUID id)
    {
        return new EntityID(id);
    }

    public String toString()
    {
        return id.toString();
    }

    public UUID toUUID()
    {
        return id;
    }
}
