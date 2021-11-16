package bitimage.domain.uploading.exceptions;

public class ImageAlreadyExistsException
        extends RuntimeException
{
    public ImageAlreadyExistsException()
    {
        super("Image with provided id already exists");
    }
}
