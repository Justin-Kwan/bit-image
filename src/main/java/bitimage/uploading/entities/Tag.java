package bitimage.uploading.entities;

import bitimage.shared.entities.Entity;
import bitimage.shared.entities.EntityID;

public class Tag
        extends Entity
{
    private final String name;

    private Tag(EntityID id, String name)
    {
        super(id);

        this.name = name;
    }

    public static Tag CreateNew(String name)
    {
        return new Tag(EntityID.CreateNew(), name);
    }

    public static Tag CreateExisting(EntityID id, String name)
    {
        return new Tag(id, name);
    }

    public String getName()
    {
        return name;
    }
}
