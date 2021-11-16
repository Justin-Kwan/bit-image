package bitimage.uploading.exceptions;

import bitimage.shared.entities.EntityID;
import bitimage.uploading.entities.FileSize;

public class ImageSizeExceededException
        extends IllegalArgumentException
{
    public ImageSizeExceededException(EntityID imageID, FileSize size)
    {
        super(String.format(
                "Image with id '%s' has size of %.2f MB, which exceeds the max upload size of 2 MB",
                imageID.toString(),
                size.toMb()));
    }
}
