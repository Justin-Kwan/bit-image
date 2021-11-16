package bitimage.uploading.exceptions;

import bitimage.shared.entities.EntityID;

public class ImageFormatInvalidException
        extends RuntimeException
{
    public ImageFormatInvalidException(EntityID imageID, String imageFormat)
    {
        super(String.format(
                "Uploaded image with id '%s' is of '%s' format, which is not supported",
                imageID.toString(),
                imageFormat));
    }
}
