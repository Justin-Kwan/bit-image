package bitimage.domain.uploading.entities;

import bitimage.domain.common.entities.EntityID;

public class FileUrl
{
    private final String url;
    private final EntityID imageID;

    public FileUrl(String url, EntityID imageID)
    {
        this.url = url;
        this.imageID = imageID;
    }

    public EntityID getImageID()
    {
        return imageID;
    }

    public String toString()
    {
        return url;
    }
}
