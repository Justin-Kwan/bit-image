package bitimage.uploading.entities;

import bitimage.shared.entities.Entity;
import bitimage.shared.entities.EntityID;

public class User
        extends Entity
{
    private final int imageUploadCount;
    private final int imageUploadLimit;

    private User(EntityID id, int imageUploadCount, int imageUploadLimit)
    {
        super(id);

        this.imageUploadCount = imageUploadCount;
        this.imageUploadLimit = imageUploadLimit;
    }

    public static User CreateNew(EntityID id)
    {
        return new User(id, 0, 10000);
    }

    public int getImageUploadCount()
    {
        return imageUploadCount;
    }

    public int getImageUploadLimit()
    {
        return imageUploadLimit;
    }

    public boolean isNull()
    {
        return false;
    }
}
